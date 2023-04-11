package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.alexirving.structs.user.User

fun Application.configureRouting() {
    controller()
    routing {
        static("/static") {
            resources("files")
        }
        authenticate {
            get("home") {
                call.respond("Welcome: ${call.authentication.principal<User>()?.identifier}")
            }

        }
        get { call.respondRedirect("/home") }


    }
}
