package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import me.alexirving.lib.database.nosql.MongoConnection
import me.alexirving.lib.database.nosql.MongoDbCachedCollection
import me.alexirving.structs.Cookie
import me.alexirving.structs.RawCookie
import me.alexirving.structs.Routine
import me.alexirving.structs.User


private val connection = MongoConnection("mongodb://localhost", "HangboardPro")

val users = MongoDbCachedCollection("Users", User::class.java, connection).getManager {
    User(it, "", listOf())
}
val routines = MongoDbCachedCollection("Routines", Routine::class.java, connection).getManager {
    Routine(it, 0, 7000, 120000)
}

val cookies = MongoDbCachedCollection("Cookies", Cookie::class.java, connection).getManager {
    Cookie(it, mutableListOf())
}


fun main(args: Array<String>): Unit = EngineMain.main(args)


suspend fun bakeCookie(id: String): String {
    val freshCookie = randomString(32)
    cookies.getOrCreate(id).cookies.add(freshCookie)
    return freshCookie
}

fun Application.module() {


    runBlocking {
        users.getOrCreate("shmezi").setPwd("12345678")
    }
    install(Authentication) {
        session<RawCookie> {
            validate { session ->
                if (cookies.getOrCreate(session.id).cookies.contains(session.cookie)) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }


    install(Sessions) {
        cookie<RawCookie>("user_session") {
            cookie.path = "/"
            val secretEncryptKey = hex("5230f3361b81f5f0c47b04d1c221a85c")
            val secretAuthKey = hex("4bcc8845033da7c215f6bad06aa48cde")
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
        }
    }
    configureRouting()

}
