package me.alexirving.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*


fun Application.api() {

    routing {
        authenticate("board") {
            route("api") {
                route("current") {
                    get {

                    }
                }


            }
        }
    }


}