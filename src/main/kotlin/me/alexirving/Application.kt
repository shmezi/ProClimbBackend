package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.netty.*
import me.alexirving.api.api
import me.alexirving.lib.database.nosql.MongoConnection
import me.alexirving.lib.database.nosql.MongoDbCachedCollection
import me.alexirving.login.Cookie
import me.alexirving.login.loginPage
import me.alexirving.structs.Routine
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import me.alexirving.structs.user.User
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder


private val connection = MongoConnection("mongodb://localhost", "HangboardPro")

val encoder: Argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

val users = MongoDbCachedCollection("Users", User::class.java, connection).getManager { id, type, params ->
    when (type) {
        "account" -> Account(id, params["password"] as String)
        "board" -> Board(id, params["password"] as String)
        else -> throw Exception("Account type does not exist!")
    }

}

val routines = MongoDbCachedCollection("Routines", Routine::class.java, connection).getManager { id, type, params ->
    Routine(id, 0, 7000, 120000)
}

val cookies = MongoDbCachedCollection("Cookies", Cookie::class.java, connection).getManager { id, type, params ->
    Cookie(id, mutableMapOf())
}


fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() {
    loginPage()
    api()
    configureRouting()

}
