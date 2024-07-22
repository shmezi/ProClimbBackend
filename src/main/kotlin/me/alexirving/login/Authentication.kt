@file:Suppress("t")

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
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import me.alexirving.api.validateUser
import me.alexirving.randomString
import me.alexirving.routinesDb
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import me.alexirving.usersDb
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


fun Route.userLogin() = route("login") {
    loginGet()
    loginPost()

}

/**
 * Authentication page UI
 */
private fun Route.loginGet() {
    get {
        if (call.sessions.get<CookiePrincipal>()?.cookie != null) {
            call.respondRedirect("/user")
            return@get
        }

        call.respond(FreeMarkerContent("/user/login.ftl", mapOf<String, String>()))
    }
}

/**
 * Authentication post login request
 */
private fun Route.loginPost() {
    post {
        val params = call.receiveParameters()

        val username = params["username"]?.lowercase()
        val password = params["password"]

        if (username == null || password == null) {
            call.respond("You did not send a username and or a password. U fool :(")
            return@post
        }

        val user = usersDb.getIfInDb(username)

        if (user == null) {
            call.respond("The username $username does not exist!")
            return@post
        }

        if (user.check(password)) {
            val cookie = bakeCookie()
            if (user is Account)
                usersDb.getIfInDb(user.identifier)?.addSession(
                    cookie,
                    SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                )
            else
                usersDb.getIfInDb(user.identifier)?.singleSession(
                    cookie,
                    SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                )
            usersDb.update(user.identifier)

            call.sessions.set(CookiePrincipal(user.identifier, cookie))
            call.respondRedirect("/user")
        } else {
            call.respondRedirect("/user/login")

        }

    }
}

/**
 * Authentication post logout request
 */
fun Route.userLogout() = get("/logout") {
    val session = call.sessions.get<CookiePrincipal>() ?: return@get
    usersDb.getIfInDb(session.id)?.removeSession(session.cookie)
    call.sessions.clear<CookiePrincipal>()
    call.respondRedirect("/user")
}

/**
 * Authentication post logoutall request
 */
fun Route.userLogoutAll() = get("/logoutall") {
    val session = call.sessions.get<CookiePrincipal>() ?: return@get
    usersDb.getIfInDb(session.id)?.clearSessions()
    call.sessions.clear<CookiePrincipal>()
    call.respondRedirect("/user")
}

/**
 * Route all the authentication
 */
fun Application.authRouting() = routing {
    route("/") {
        get {
            call.respondRedirect("/user")
        }
    }
    route("/user") {
        userLogin()
        userLogout()
        userLogoutAll()

        authAccount()
        userSignup()
        userProfile()


    }

}

/**
 * Registration page UI
 */
private fun Route.userProfile() {
    authenticate("board", "account", strategy = AuthenticationStrategy.FirstSuccessful) {
        get {
            val user = usersDb.getIfInDb(call.sessions.get<CookiePrincipal>()?.id ?: "Not found")
            if (user == null) {
                call.respond("User was not found!")
                return@get
            }
            val map = mutableMapOf<String, String>()
            map["username"] = user.identifier
            map["type"] = if (user is Board) "Board" else "Standard"
            if (user is Account) {
                call.respond(FreeMarkerContent("/user/profile.ftl", map))
            } else {
                call.respond(FreeMarkerContent("/user/board-profile.ftl", map))
            }
        }
    }
}

/**
 * Registration post login request
 */
private fun Route.userSignup() {
    route("/signup") {
        get {
            call.respond(FreeMarkerContent("/user/signup.ftl", mapOf<String, String>()))
        }
        post {
            val params = call.receiveParameters()
            val password = params["password"]
            if (password == null) {
                call.respond("No password provided!")
                return@post
            }
            val username = params["username"]?.lowercase()
            if (username == null) {
                call.respond("No username provided!")
                return@post
            }
            val type = params["type"] ?: "account"
            if (usersDb.getIfInDb(username) != null) {
                call.respond("Username already exists!")
                return@post
            } else {
                val cookie = bakeCookie()
                usersDb.getOrCreate(
                    username,
                    type,
                    mutableMapOf<String, Any>().apply { this["password"] = password }) {
                    setPassword(password)
                    addSession(
                        cookie,
                        SessionData(Date.from(Instant.now()).toString(), call.request.origin.remoteAddress)
                    )
                }
                call.sessions.set(CookiePrincipal(username, cookie))
                call.respondRedirect("/user")
            }
        }
    }
}

private fun Route.authAccount() {
    authenticate("account") {
        get("/logs", logsUI())
    }
}

private fun logsUI(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        call.respondHtml {
            head {
                title { +"ProHang | Logs" }
                link(rel = "stylesheet", href = "/static/styles/logs.css")
            }
            body {
                val user = call.principal<Account>()
                h1 { +"User logs:" }
                for (log in user?.logs ?: listOf()) {
                    runBlocking {
                        val routine = routinesDb.getIfInDb(log.routineId) ?: return@runBlocking
                        section(classes = "container") {
                            div(classes = "card") {
                                div(classes = "image") {
                                    img(src = routine.icon) { alt = "" }
                                }
                                h2 { +"Routine: ${routine.name}" }
                                p { +"${log.date}: - ${if (log.success) "Success!" else "Failed :("}" }
                            }
                        }
                    }
                }
                div {
                    a(href = "/user") { +"Back to profile." }
                }
            }
        }
    }

fun Application.configureAuth() = install(Authentication) {
    session<CookiePrincipal>("account") {
        validate { session ->
            val user = usersDb.getIfInDb(session.id)
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
            val user = usersDb.getIfInDb(session.id)
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
            usersDb.getIfInDb(credentials.name.lowercase())?.apply {
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

fun Application.configureSessions() = install(Sessions) {
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


fun Application.loginPage() {
    configureAuth()
    configureSessions()
    authRouting()

}