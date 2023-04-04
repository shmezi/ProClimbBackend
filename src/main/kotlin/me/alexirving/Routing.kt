package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        static("/static") {
            resources("files")
        }
        authenticate {
            get("home") {
                call.respond("Nicely logged in!")
            }

        }
        get { call.respondRedirect("/home") }


    }
}
