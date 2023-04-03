package me.alexirving.structs

import io.ktor.server.auth.*
import kotlinx.coroutines.runBlocking
import me.alexirving.lib.database.core.Cacheable
import me.alexirving.users
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder


private val encoder: Argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

class User(identifier: String, private var pwd: String, log: List<UserLog>) : Cacheable<String>(identifier), Principal {
    fun setPwd(newPassword: String) {
        pwd = encoder.encode(newPassword)
    }

    fun check(toCheck: String) = encoder.matches(toCheck, pwd)

    companion object {
        fun encode(id: String, pwd: String) = User(id, encoder.encode(pwd), listOf())

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