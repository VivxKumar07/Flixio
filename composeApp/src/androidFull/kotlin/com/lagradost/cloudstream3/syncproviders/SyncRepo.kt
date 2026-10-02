package com.lagradost.cloudstream3.syncproviders

open class SyncRepo {
    open val name: String = ""
    open val mainUrl: String = ""
    open val icon: Int? = null
    open val requiresLogin: Boolean = false
    open val idPrefix: String = ""
}
