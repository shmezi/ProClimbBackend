package me.alexirving.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javafx.concurrent.WorkerStateEvent


fun Application.api() {

    routing {
        authenticate {
            route("api") {
                route("current"){
                    get {




                    }
                }
            }
        }
    }


}