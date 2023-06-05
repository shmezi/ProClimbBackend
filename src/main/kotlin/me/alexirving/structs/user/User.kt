package me.alexirving.structs.user

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import me.alexirving.encoder
import me.alexirving.lib.database.core.Cacheable
import me.alexirving.login.SessionData
import me.alexirving.structs.Routine

@Serializable
sealed class User : Cacheable<String>, Principal {
    abstract var pwd: String
    val sessions = mutableMapOf<String, SessionData>()
    val routines = mutableMapOf<String, Routine>()
    fun setPassword(newPassword: String) {
        pwd = encoder.encode(newPassword)
    }

    fun check(toCheck: String) = encoder.matches(toCheck, pwd)

    fun addSession(cookie: String, data: SessionData) {
        sessions[cookie] = data
    }

    fun clearSessions() = sessions.clear()
    fun singleSession(cookie: String, data: SessionData) {
        clearSessions(); addSession(cookie, data)
    }

    fun removeSession(cookie: String) = sessions.remove(cookie) ?: false

    fun isSession(cookie: String) = sessions.keys.contains(cookie)

}