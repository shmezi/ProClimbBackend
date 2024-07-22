package me.alexirving

import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.runBlocking
import me.alexirving.api.api
import me.alexirving.api.configureController
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
import java.io.File
import java.io.FileInputStream
import java.util.*


private lateinit var connection: MongoConnection

val encoder: Argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

/**
 * UserId: User
 */
lateinit var usersDb: CachedDbManager<String, User>

/**
 * RoutineID: Routine
 */
lateinit var routinesDb: CachedDbManager<String, Routine>


fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() = runBlocking {
    copyOver("config.properties")
    val props = Properties()
    props.load(FileInputStream("config.properties"))
    connection = MongoConnection(
        props.getProperty("CONNECTION") ?: throw NullPointerException("connection not found!"),
        "HangboardPro"
    )
    routing {
        staticFiles("/static", File("files"))
    }

    install(WebSockets)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    loginPage()
    configureController()
    api()

    routinesDb = MongoDbCachedCollection("Routines", Routine::class.java, connection).getManager { id, _, _ ->
        Routine(id, id, "https://cdn-icons-png.flaticon.com/512/5500/5500903.png", "Default", 7, 3, 6, 180, 5)
    }

    val default = routinesDb.getOrCreate("default") {
        name = "Normal"
        description = "A nice calm routine"
        icon = "https://cdn-icons-png.flaticon.com/512/6643/6643177.png"
    }
    val speed = routinesDb.getOrCreate("fast") {
        name = "Fast"
        description = "A super speedy workout!"
        icon = "https://cdn-icons-png.flaticon.com/512/1455/1455318.png"
        this.hangTime = 2
        this.numberOfSets = 3
        this.pauseTime = 2
        this.restTime = 5
        this.roundCount = 2
    }
    val balanced = routinesDb.getOrCreate("balanced") {
        name = "Balanced"
        description = "A balanced routine"
        icon = "https://cdn-icons-png.flaticon.com/512/2994/2994536.png"
        this.hangTime = 4
        this.numberOfSets = 5
        this.pauseTime = 4
        this.restTime = 7
        this.roundCount = 4
    }
    usersDb = MongoDbCachedCollection("Users", User::class.java, connection).getManager { id, type, params ->
        when (type) {
            "account" -> Account(
                id,
                params["password"] as String? ?: throw NullPointerException("No password found"),
                mutableListOf()
            )

            "board" -> Board(id, params["password"] as String).apply {
                routines["speed"] = speed
                routines["balanced"] = balanced
                routines["default"] = default
            }

            else -> throw NullPointerException("Cannot find user type")
        }

    }



}
