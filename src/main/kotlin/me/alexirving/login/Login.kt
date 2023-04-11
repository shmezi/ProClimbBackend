package me.alexirving.login

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import me.alexirving.randomString
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import me.alexirving.structs.user.User
import me.alexirving.users
import java.time.Instant
import java.util.*
import kotlin.collections.set


fun bakeCookie() = randomString(32)
fun Application.loginPage() {
    install(Authentication) {
        session<RawCookie> {
            validate { session ->
                val user = users.getIfInDb(session.id)
                user?.isSession(session.cookie)?.apply {
                    if (user is Account)
                        return@validate user
                }
                this.sessions.clear<RawCookie>()
                null
            }
            challenge {
                call.respondRedirect("/user/login")
            }
        }
        session<RawCookie>("board") {
            validate { session ->
                val user = users.getIfInDb(session.id)
                user?.isSession(session.cookie)?.apply {
                    if (user is Board)
                        return@validate user
                }
                this.sessions.clear<RawCookie>()
                null
            }
            challenge {
                call.respondRedirect("/user/login")
            }
        }
        session<RawCookie>() {
            validate { session ->


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


    routing {

        route("user") {
            route("login") {
                get {
                    if (call.sessions.get<RawCookie>()?.cookie != null) {
                        call.respondRedirect("/home")
                        return@get
                    }

                    call.respondHtml {
                        body {
                            form(
                                action = "/user/login",
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
                                a {
                                    href = "/user/signup"
                                    +"Signup!"
                                }
                            }
                        }
                    }
                }

                post {
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
                        val cookie = bakeCookie()
                        users.getOrCreate(user.identifier, "account") {
                            addSession(
                                cookie,
                                SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                            )
                        }
                        call.sessions.set(RawCookie(user.identifier, cookie))
                        call.respondRedirect("/home")
                    } else {
                        call.respond("Incorrect password!")

                    }

                }

            }
            get("/logout") {
                val session = call.sessions.get<RawCookie>() ?: return@get
                users.getOrCreate(session.id, "account") {
                    sessions?.remove(session.cookie)
                }
                call.sessions.clear<RawCookie>()
                call.respondRedirect("/home")
            }

            route("/signup") {
                get {
                    call.respondHtml {
                        body {
                            form(
                                action = "/user/signup",
                                encType = FormEncType.applicationXWwwFormUrlEncoded,
                                method = FormMethod.post
                            ) {
                                p {
                                    +"Username:"
                                    textInput(name = "username")

                                }
                                p {
                                    +"Password:"
                                    passwordInput(name = "password1")

                                }
                                p {
                                    +"Repeat password:"
                                    passwordInput(name = "password2")
                                }

                                p {
                                    submitInput { value = "Signup" }
                                }
                                select {
                                    name = "type"
                                    option {
                                        value = "account"
                                        +"Standard"
                                    }
                                    option {
                                        value = "board"
                                        +"Hangboard-owner"
                                    }
                                }
                                a {
                                    href = "/user/login"
                                    +"Login instead."
                                }
                            }
                        }
                    }

                }
                post {
                    val params = call.receiveParameters()
                    val passwordA = params["password1"]

                    if (
                        passwordA != params["password2"] || passwordA == null
                    ) {
                        call.respond("Passwords do not match!")
                        return@post
                    }

                    val username = params["username"]
                    if (username == null) {
                        call.respond("No username provided!")
                        return@post
                    }
                    val type = params["type"]
                    if (type == null) {
                        call.respond("No type provided!")
                        return@post
                    }
                    if (users.getIfInDb(username) != null) {
                        call.respond("Username already exists!")
                        return@post

                    } else {
                        users.getOrCreate(
                            username,
                            type,
                            mutableMapOf<String, Any>().apply { this["password"] = passwordA }) {
                            setPassword(passwordA)
                        }
                        val cookie = bakeCookie()
                        users.getOrCreate(
                            username,
                            "account",
                            mutableMapOf<String, Any>().apply { this["password"] = passwordA }) {
                            addSession(
                                cookie,
                                SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                            )
                        }
                        call.sessions.set(RawCookie(username, cookie))
                        call.respondRedirect("/home")
                    }

                }


            }

            get {

                val user = users.getIfInDb(call.sessions.get<RawCookie>()?.id ?: "Not found")
                if (user == null) {
                    call.respond("User was not found!")
                    return@get
                }
                call.respondHtml {
                    body {
                        h1 { +"Account info:" }
                        p { +"Username: ${user.identifier}" }
                        fun type(u: User): String {
                            return when (u) {
                                is Account -> "Account"
                                is Board -> "Board"
                            }
                        }

                        p { +"Account type: ${type(user)}" }
                        a {
                            href = "/user/logout"
                            +"Sign out"

                        }
                        h2 {
                            +"Logged in sessions:"
                        }
                        ul {
                            for (x in user.sessions.values) {
                                li {

                                    +"Date: ${x.date}| Ip: ${x.ip}"
                                }
                            }

                        }


                    }


                }


            }

        }

    }


}