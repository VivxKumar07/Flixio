package com.nuvio.app.core.auth

import co.touchlab.kermit.Logger
import com.nuvio.app.core.network.SupabaseProvider
import com.nuvio.app.core.storage.LocalAccountDataCleaner
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import com.nuvio.app.core.network.ServerConfigurationRepository

object AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("AuthRepository")

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var initialized = false
    private var sessionStatusJob: Job? = null
    private var remoteValidationJob: Job? = null
    private var validatingRemoteUserId: String? = null
    private var validatedRemoteUserId: String? = null

    fun initialize() {
        if (initialized) return
        initialized = true

        val currentSession = runCatching { SupabaseProvider.client.auth.currentSessionOrNull() }.getOrNull()
        if (currentSession?.user != null) {
            AuthStorage.clearAnonymousUserId()
            val userId = currentSession.user!!.id
            _state.value = AuthState.Authenticated(
                userId = userId,
                email = currentSession.user?.email,
                isAnonymous = false,
            )
            validateRemoteSessionInBackground(userId)
        } else {
            val savedAnonId = AuthStorage.loadAnonymousUserId()
            if (savedAnonId != null) {
                _state.value = AuthState.Authenticated(
                    userId = savedAnonId,
                    email = null,
                    isAnonymous = true,
                )
            }
        }

        sessionStatusJob = scope.launch {
            SupabaseProvider.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        AuthStorage.clearAnonymousUserId()
                        val user = status.session.user
                        val userId = user?.id.orEmpty()
                        _state.value = AuthState.Authenticated(
                            userId = userId,
                            email = user?.email,
                            isAnonymous = false,
                        )
                        validateRemoteSessionInBackground(userId)
                    }
                    is SessionStatus.NotAuthenticated -> {
                        val savedAnonId = AuthStorage.loadAnonymousUserId()
                        if (savedAnonId != null) {
                            _state.value = AuthState.Authenticated(
                                userId = savedAnonId,
                                email = null,
                                isAnonymous = true,
                            )
                        } else {
                            _state.value = AuthState.Unauthenticated
                        }
                    }
                    is SessionStatus.Initializing -> {
                        val existingSession = runCatching { SupabaseProvider.client.auth.currentSessionOrNull() }.getOrNull()
                        if (existingSession?.user != null) {
                            AuthStorage.clearAnonymousUserId()
                            val userId = existingSession.user!!.id
                            _state.value = AuthState.Authenticated(
                                userId = userId,
                                email = existingSession.user?.email,
                                isAnonymous = false,
                            )
                        } else if (AuthStorage.loadAnonymousUserId() == null) {
                            _state.value = AuthState.Loading
                        }
                    }
                    is SessionStatus.RefreshFailure -> {
                        val savedAnonId = AuthStorage.loadAnonymousUserId()
                        if (savedAnonId != null) {
                            _state.value = AuthState.Authenticated(
                                userId = savedAnonId,
                                email = null,
                                isAnonymous = true,
                            )
                        } else {
                            _state.value = AuthState.Unauthenticated
                        }
                    }
                }
            }
        }
    }

    /**
     * Session restoration already verifies the locally persisted token.  Do the optional
     * remote account check after exposing that restored session so a slow network cannot hold
     * the whole app at its launch gate.  Invalid sessions still follow the existing cleanup
     * path, while transient failures retain the restored session as before.
     */
    private fun validateRemoteSessionInBackground(userId: String) {
        if (
            userId.isBlank() ||
            validatedRemoteUserId == userId ||
            (validatingRemoteUserId == userId && remoteValidationJob?.isActive == true)
        ) return

        validatingRemoteUserId = userId
        remoteValidationJob?.cancel()
        remoteValidationJob = scope.launch {
            try {
                validateRemoteSession(userId)
            } finally {
                if (validatingRemoteUserId == userId) {
                    validatingRemoteUserId = null
                }
            }
        }
    }

    private suspend fun validateRemoteSession(userId: String): Boolean {
        if (userId.isBlank() || validatedRemoteUserId == userId) return true
        if ((_state.value as? AuthState.Authenticated)?.userId != userId) return false

        return runCatching {
            SupabaseProvider.client.auth.retrieveUserForCurrentSession(false)
            if ((_state.value as? AuthState.Authenticated)?.userId != userId) return false
            validatedRemoteUserId = userId
            true
        }.getOrElse { e ->
            if (isInvalidRemoteSessionError(e)) {
                if ((_state.value as? AuthState.Authenticated)?.userId != userId) return false
                log.w(e) { "Stored Supabase session no longer belongs to an active account; clearing local auth" }
                clearLocalSessionAfterRemoteInvalidation()
                false
            } else {
                log.w(e) { "Unable to validate stored Supabase session; keeping cached auth state" }
                true
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun signInAnonymously() {
        _error.value = null
        val userId = Uuid.random().toString()
        AuthStorage.saveAnonymousUserId(userId)
        _state.value = AuthState.Authenticated(
            userId = userId,
            email = null,
            isAnonymous = true,
        )
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = runCatching {
        _error.value = null
        val config = ServerConfigurationRepository.active.value
        if (config.backendUrl.isBlank()) {
            val message = "Authentication server is not configured. Use 'Continue without account' to use Flixio locally."
            _error.value = message
            error(message)
        }
        AuthStorage.clearAnonymousUserId()
        SupabaseProvider.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }.onFailure { e ->
        log.e(e) { "Email sign-up failed" }
        if (_error.value == null) {
            _error.value = e.safeAuthErrorDescription()
                ?: e.message?.takeIf { it.isNotBlank() }
                ?: getString(Res.string.auth_sign_up_failed)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        _error.value = null
        val config = ServerConfigurationRepository.active.value
        if (config.backendUrl.isBlank()) {
            val message = "Authentication server is not configured. Use 'Continue without account' to use Flixio locally."
            _error.value = message
            error(message)
        }
        AuthStorage.clearAnonymousUserId()
        SupabaseProvider.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }.onFailure { e ->
        log.e(e) { "Email sign-in failed" }
        if (_error.value == null) {
            _error.value = e.safeAuthErrorDescription()
                ?: e.message?.takeIf { it.isNotBlank() }
                ?: getString(Res.string.auth_sign_in_failed)
        }
    }

    suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        _error.value = null
        AuthStorage.clearAnonymousUserId()
        SupabaseProvider.client.auth.signInWith(Google)
    }.onFailure { error ->
        log.e(error) { "Google sign-in could not be started" }
        _error.value = error.safeAuthErrorDescription()
            ?: error.message?.takeIf { it.isNotBlank() }
            ?: "Google sign-in could not be started."
    }

    suspend fun signOut(): Result<Unit> {
        _error.value = null
        val anonymousRead = runCatching { AuthStorage.loadAnonymousUserId() }
        val wasAnonymous = anonymousRead.getOrNull() != null
        val anonymousClear = runCatching { AuthStorage.clearAnonymousUserId() }
        validatedRemoteUserId = null
        val remoteSignOut = if (wasAnonymous) {
            Result.success(Unit)
        } else {
            runCatching { SupabaseProvider.client.auth.signOut() }
        }

        val fallbackSessionClear = if (remoteSignOut.isFailure) {
            runCatching { SupabaseProvider.client.auth.clearSession() }
                .onFailure { error -> log.w(error) { "Failed to clear Supabase session after sign-out failure" } }
        } else {
            Result.success(Unit)
        }
        val localCleanup = runCatching { LocalAccountDataCleaner.wipe() }
        _state.value = AuthState.Unauthenticated

        val failure = anonymousRead.exceptionOrNull()
            ?: anonymousClear.exceptionOrNull()
            ?: remoteSignOut.exceptionOrNull()
            ?: fallbackSessionClear.exceptionOrNull()
            ?: localCleanup.exceptionOrNull()
        val cancellation = remoteSignOut.exceptionOrNull() as? CancellationException
            ?: fallbackSessionClear.exceptionOrNull() as? CancellationException
        if (cancellation != null) throw cancellation
        return if (failure == null) {
            Result.success(Unit)
        } else {
            log.e(failure) { "Sign-out did not complete cleanly; all local cleanup steps were attempted" }
            _error.value = failure.message ?: runCatching {
                getString(Res.string.auth_sign_out_failed)
            }.getOrDefault("Sign out failed")
            Result.failure(failure)
        }
    }

    suspend fun prepareForServerSwitch(): Result<Unit> {
        _error.value = null
        val anonymousClear = runCatching { AuthStorage.clearAnonymousUserId() }
        validatedRemoteUserId = null
        val sessionClear = runCatching { SupabaseProvider.client.auth.clearSession() }
        _state.value = AuthState.Unauthenticated
        val failure = anonymousClear.exceptionOrNull() ?: sessionClear.exceptionOrNull()
        val cancellation = sessionClear.exceptionOrNull() as? CancellationException
        if (cancellation != null) throw cancellation
        return if (failure == null) Result.success(Unit) else Result.failure(failure)
    }

    fun reinitialize() {
        sessionStatusJob?.cancel()
        sessionStatusJob = null
        remoteValidationJob?.cancel()
        remoteValidationJob = null
        validatingRemoteUserId = null
        initialized = false
        validatedRemoteUserId = null
        _state.value = AuthState.Loading
        initialize()
    }

    suspend fun signOutIfSessionInvalid(error: Throwable, source: String): Boolean {
        if (!isInvalidRemoteSessionError(error)) return false

        log.w(error) { "$source failed because the current Supabase account/session is no longer valid; clearing local auth" }
        clearLocalSessionAfterRemoteInvalidation()
        return true
    }

    private suspend fun clearLocalSessionAfterRemoteInvalidation() {
        _error.value = null
        AuthStorage.clearAnonymousUserId()
        validatedRemoteUserId = null
        runCatching {
            SupabaseProvider.client.auth.clearSession()
        }.onFailure { e ->
            log.w(e) { "Failed to clear Supabase session after remote invalidation; continuing local reset" }
        }
        val localCleanup = runCatching { LocalAccountDataCleaner.wipe() }
        _state.value = AuthState.Unauthenticated
        localCleanup.onFailure { error ->
            log.e(error) { "Local account cleanup failed after remote session invalidation" }
        }
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        _error.value = null
        SupabaseProvider.client.functions.invoke("delete-account")
        SupabaseProvider.client.auth.signOut()
        validatedRemoteUserId = null
        try {
            LocalAccountDataCleaner.wipe()
        } finally {
            _state.value = AuthState.Unauthenticated
        }
    }.onFailure { e ->
        log.e(e) { "Account deletion failed" }
        _error.value = e.message ?: getString(Res.string.auth_account_deletion_failed)
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }

    private fun isInvalidRemoteSessionError(error: Throwable): Boolean {
        val restError = error.findCause<RestException>()
        if (restError?.statusCode == 401 || restError?.statusCode == 403) return true

        val message = buildString {
            append(error.message.orEmpty())
            if (restError != null) {
                append(' ')
                append(restError.error)
                append(' ')
                append(restError.description)
            }
        }.lowercase()

        return (
            "jwt" in message &&
                ("invalid" in message || "expired" in message || "malformed" in message)
            ) || (
            "user" in message &&
                ("does not exist" in message || "not found" in message || "deleted" in message)
            ) || (
            "foreign key" in message &&
                ("auth.users" in message || "user_id" in message)
            )
    }

    private inline fun <reified T : Throwable> Throwable.findCause(): T? {
        var current: Throwable? = this
        while (current != null) {
            if (current is T) return current
            current = current.cause
        }
        return null
    }

    private fun Throwable.safeAuthErrorDescription(): String? =
        findCause<AuthRestException>()
            ?.errorDescription
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: findCause<RestException>()
                ?.description
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
}
