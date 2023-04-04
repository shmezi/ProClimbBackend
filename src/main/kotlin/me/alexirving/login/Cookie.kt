package me.alexirving.login

import io.ktor.server.auth.*
import me.alexirving.lib.database.core.Cacheable

class Cookie(
    identifier: String,
    var sessions: MutableMap<String, SessionData>?
) : Principal, Cacheable<String>(identifier) {


    fun addSession(cookie: String, data: SessionData) {
        if (sessions == null)
            sessions = mutableMapOf()
        sessions?.put(cookie, data)
    }

    fun removeSession(cookie: String) = sessions?.remove(cookie) ?: false

    fun isSession(cookie: String) = sessions?.keys?.contains(cookie) ?: false
}