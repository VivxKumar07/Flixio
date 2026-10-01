package io.github.up9cloud.td

object JsonClient {
    private var loaded = false

    init {
        try {
            System.loadLibrary("tdjson")
            loaded = true
        } catch (t: Throwable) {
            t.printStackTrace()
            loaded = false
        }
    }

    @JvmStatic
    fun isLoaded(): Boolean = loaded

    @JvmStatic
    external fun td_create_client_id(): Int

    @JvmStatic
    external fun td_send(clientId: Int, request: String)

    @JvmStatic
    external fun td_receive(timeout: Double): String?

    @JvmStatic
    external fun td_execute(request: String): String?
}
