package me.alexirving.structs.user

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.ktor.server.auth.*
import kotlinx.coroutines.runBlocking
import me.alexirving.encoder
import me.alexirving.lib.database.core.Cacheable
import me.alexirving.users


abstract class User(identifier: String, var pwd: String) : Cacheable<String>(identifier), Principal {
    fun setPassword(newPassword: String) {
        pwd = encoder.encode(newPassword)
    }

    fun check(toCheck: String) = encoder.matches(toCheck, pwd)

    companion object {


        /**
         * Checks if a password is correct based on the encoded password
         */
        fun check(toCheck: String, real: String) = encoder.matches(toCheck, real)

        /**
         * Checks if a password is the correct password for a user.
         */
        fun checkUser(user: String, pwd: String) = runBlocking {
            check(pwd, users.getIfInDb(user)?.pwd ?: return@runBlocking false)
        }

    }

}