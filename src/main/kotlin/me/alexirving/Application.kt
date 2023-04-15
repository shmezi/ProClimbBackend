package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.runBlocking
import me.alexirving.api.api
import me.alexirving.lib.database.nosql.MongoConnection
import me.alexirving.lib.database.nosql.MongoDbCachedCollection
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
        "account" -> Account(id, params["password"] as String? ?: throw NullPointerException("OOPS"), mutableListOf())
        "board" -> Board(id, params["password"] as String)
        else -> throw Exception("Account type does not exist!")
    }

}

val routines = MongoDbCachedCollection("Routines", Routine::class.java, connection).getManager { id, _, _ ->
    Routine(id, 7, 3, 6, 180, 5)
}


fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() = runBlocking {
    install(WebSockets)
    loginPage()
    api()
    configureRouting()
    routines.getOrCreate("default") {
    }
    routines.getOrCreate("fast") {
        this.hangTime = 2
        this.numberOfSets = 3
        this.pauseTime = 2
        this.restTime = 5
        this.roundCount = 2


    }
}
