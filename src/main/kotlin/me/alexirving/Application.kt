package me.alexirving

import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.alexirving.api.api
import me.alexirving.lib.database.manager.CachedDbManager
import me.alexirving.lib.database.nosql.MongoConnection
import me.alexirving.lib.database.nosql.MongoDbCachedCollection
import me.alexirving.lib.util.copyOver
import me.alexirving.login.loginPage
import me.alexirving.structs.Routine
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import me.alexirving.structs.user.User
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.io.FileInputStream
import java.util.*


private lateinit var connection: MongoConnection

val encoder: Argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

lateinit var users: CachedDbManager<String, User>

lateinit var routines: CachedDbManager<String, Routine>


fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() = runBlocking {
    copyOver("config.properties")
    val props = Properties()
    props.load(FileInputStream("config.properties"))
    connection = MongoConnection(props.getProperty("CONNECTION") ?: throw NullPointerException("connection not found!"), "HangboardPro")

    users = MongoDbCachedCollection("Users", User::class.java, connection).getManager { id, type, params ->
        when (type) {
            "account" -> Account(
                id,
                params["password"] as String? ?: throw NullPointerException("OOPS"),
                mutableListOf()
            )

            "board" -> Board(id, params["password"] as String)
            else -> throw Exception("Account type does not exist!")
        }

    }
    routines = MongoDbCachedCollection("Routines", Routine::class.java, connection).getManager { id, _, _ ->
        Routine(id, 7, 3, 6, 180, 5)
    }
    install(WebSockets)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
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
