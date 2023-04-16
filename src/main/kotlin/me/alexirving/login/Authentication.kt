package me.alexirving.login

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import me.alexirving.api.validateUser
import me.alexirving.randomString
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import me.alexirving.structs.user.User
import me.alexirving.users
import java.time.Instant
import java.util.*
import kotlin.collections.set


fun bakeCookie() = randomString(32)

fun validPassword(username: String, password: String): Boolean {
    return when {
        password.length < 8 || username.length > 16 -> false
        password == username -> false
        else -> true
    }
}

val regex = "^[a-zA-Z0-9 ]*\$\n".toRegex()

fun validUsername(username: String): Boolean {
    return when {
        username.length < 3 || username.length > 16 -> false
        !username.matches(regex) -> false
        else -> true
    }
}

fun Application.loginPage() {
    install(Authentication) {
        session<CookiePrincipal>("account") {
            validate { session ->
                val user = users.getIfInDb(session.id)
                user?.isSession(session.cookie)?.apply {
                    if (user is Account)
                        return@validate user
                }
                null
            }
            challenge {
                call.respondRedirect("/user/login")
            }
        }
        session<CookiePrincipal>("board") {
            validate { session ->
                val user = users.getIfInDb(session.id)
                user?.isSession(session.cookie)?.apply {
                    if (user is Board)
                        return@validate user
                }
                null
            }
            challenge {
                call.respondRedirect("/user/login")
            }
        }
        basic("board-api") {
            validate { credentials ->
                users.getIfInDb(credentials.name)?.apply {
                    if (this !is Board)
                        return@validate null
                    if (check(credentials.password)) {
                        return@validate this
                    } else
                        return@validate null
                }
                null
            }
        }
        session<BoardSessionPrinciple>("session") {
            validate { session ->
                validateUser(session.code)
            }
            challenge("/control/auth")
        }
    }

    install(Sessions) {
        cookie<CookiePrincipal>("user_session") {
            cookie.path = "/"
            val secretEncryptKey = hex("5230f3361b81f5f0c47b04d1c221a85c")
            val secretAuthKey = hex("4bcc8845033da7c215f6bad06aa48cde")
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
        }
        cookie<BoardSessionPrinciple>("user-board_session") {
            cookie.path = "/"
            val secretEncryptKey = hex("4bcc8845033da7c215f6bad06aa48cde")
            val secretAuthKey = hex("5230f3361b81f5f0c47b04d1c221a85c")
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
        }
    }


    routing {
        route("/user") {
            route("login") {
                get {
                    if (call.sessions.get<CookiePrincipal>()?.cookie != null) {
                        call.respondRedirect("/home")
                        return@get
                    }
                    call.respond(FreeMarkerContent("/user/login.ftl", mapOf<String, String>()))
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
                        if (user is Account)
                            users.getIfInDb(user.identifier)?.addSession(
                                cookie,
                                SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                            )
                        else
                            users.getIfInDb(user.identifier)?.singleSession(
                                cookie,
                                SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                            )
                        users.update(user.identifier)

                        call.sessions.set(CookiePrincipal(user.identifier, cookie))
                        call.respondRedirect("/user")
                    } else {
                        call.respondRedirect("/user/login")

                    }

                }

            }
            get("/logout") {
                val session = call.sessions.get<CookiePrincipal>() ?: return@get
                users.getIfInDb(session.id)?.removeSession(session.cookie)
                call.sessions.clear<CookiePrincipal>()
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
                        call.sessions.set(CookiePrincipal(username, cookie))
                        call.respondRedirect("/home")
                    }

                }


            }
            authenticate("board", "account", strategy = AuthenticationStrategy.FirstSuccessful) {
                get {

                    val user = users.getIfInDb(call.sessions.get<CookiePrincipal>()?.id ?: "Not found")
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


}