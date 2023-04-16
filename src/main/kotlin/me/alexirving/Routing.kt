package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.alexirving.api.controller
import me.alexirving.structs.user.User

fun Application.configureRouting() {
    controller()
    routing {

        static("/") {
            resources("files")
        }
        authenticate("account", "board", strategy = AuthenticationStrategy.FirstSuccessful) {
            get("home") {
                val user = call.principal<User>() ?: return@get
                call.respond(FreeMarkerContent("index.ftl", mapOf( "user" to user)))
            }
        }
        get { call.respondRedirect("/home") }


    }
}
