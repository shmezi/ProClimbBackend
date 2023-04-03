package me.alexirving

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import me.alexirving.lib.util.pq
import me.alexirving.structs.Cookie
import me.alexirving.structs.RawCookie

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


        route("login") {
            get {
                call.respondHtml {
                    body {
                        form(
                            action = "/login",
                            encType = FormEncType.applicationXWwwFormUrlEncoded,
                            method = FormMethod.post
                        ) {
                            p {
                                +"Username:"
                                textInput(name = "username")

                            }
                            p {
                                +"Password:"
                                passwordInput(name = "password")
                            }
                            p {
                                submitInput { value = "Login" }
                            }
                        }
                    }
                }
            }
            post {
                "wew".pq()
                val params = call.receiveParameters()

                val username = params["username"]
                val password = params["password"]

                if (username == null || password == null) {
                    call.respond("You did not send a username and or a password. U fool :(")
                    return@post
                }


                val user = users.getIfInDb(username)

                if (user == null) {
                    call.respond("The username $username does not exist!")
                    return@post
                }

                if (user.check(password)) {
                    call.sessions.set(RawCookie(user.identifier, bakeCookie(user.identifier)))
                    call.respondRedirect("/home")
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond("Incorrect password!")

                    call.respond(HttpStatusCode.NotAcceptable)
                }

            }

        }

        get("/cart") {
            val cartSession = call.sessions.get<Cookie>()
            if (cartSession != null) {
                call.respondText("Product IDs: ${cartSession.identifier}")
            } else {
                call.respondText("Your basket is empty.")
            }
        }

        get("/logout") {
            val session = call.sessions.get<RawCookie>() ?: return@get
            cookies.getOrCreate(session.id).cookies.remove(session.cookie)
            call.sessions.clear<Cookie>()
            call.respondRedirect("/cart")
        }


    }
}
