package me.alexirving.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import me.alexirving.lib.util.pq
import me.alexirving.login.BoardSessionPrinciple
import me.alexirving.login.CookiePrincipal
import me.alexirving.randomString
import me.alexirving.routinesDb
import me.alexirving.structs.Routine
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import me.alexirving.usersDb
import kotlin.collections.set

//Code: Board  | Board: Code
private val codeBoard = mutableMapOf<String, Board>()
private val boardCode = mutableMapOf<Board, String>()

//Code: UserID
private val inUse = mutableMapOf<String, Board>()


/**
 * Load a board into the available to connect list with a new code
 * @param board to connect to
 */
fun loadBoard(board: Board): String {
    val code = randomString(6)
    codeBoard[code] = board
    boardCode[board] = code
    return code

}

/**
 * Connect user to a board.
 * @param code The code to connect to
 * @param user The user that will connect to the board.
 */
fun loginToBoard(code: String, user: Account?): Board? {
    if (inUse.containsKey(code)) return null
    val board = codeBoard[code] ?: return null
    inUse[code] = board
    board.instance.connect(user)
    return board
}

/**
 * Validate weather a user is using a board and return it
 * @param code Code of the board
 * @return The board from the code provided
 */
fun validateUser(code: String): Board? {
    return inUse[code]
}

/**
 * Destroy a user's session.
 */
fun killUserSession(board: Board) = inUse.remove(boardCode[board])

fun getCode(board: Board) = boardCode[board]


fun Application.controller() {
    routing {
        route("create") {
            authenticate("board", "account", strategy = AuthenticationStrategy.FirstSuccessful) {
                post {
                    val user = usersDb.getIfInDb(call.sessions.get<CookiePrincipal>()?.id ?: "Not found")
                    val p = call.parameters
                    user?.routines?.set(
                        p["routine"].toString(),
                        Routine(
                            p["routine"].toString(),
                            p["routine"].toString(),
                            p["icon"].toString(),
                            p["description"].toString(),
                            p["hangTime"]?.toInt() ?: 0,
                            p["pauseTime"]?.toInt() ?: 0,
                            p["roundCount"]?.toInt() ?: 0,
                            p["restTime"]?.toInt() ?: 0,
                            p["numberOfSets"]?.toInt() ?: 0
                        )
                    )
                    usersDb.update(user?.identifier ?: return@post)
                }

                get {
                    call.respond(FreeMarkerContent("/controller/create.ftl", mapOf<String, String>()))
                }
            }
        }
        route("control") {
            authenticate("session", "account", strategy = AuthenticationStrategy.Required) {
                get {

                    val routines = mutableListOf<Routine>()
                    val user = call.principal<Account>()
                    val board = call.principal<Board>()
                    user?.routines?.forEach { routines.add(it.value) }
                    board?.routines?.forEach { routines.add(it.value) }
                    call.respondHtml {
                        head {
                            title { +"ProHang | Controller" }
                            link(rel = "stylesheet", href = "/static/styles/controller.css")

                        }
                        body {
                            h1 { +"Select a routine:" }
                            h3 { +"Connected to: ${board?.identifier}" }
                            form(
                                action = "/control",
                                encType = FormEncType.applicationXWwwFormUrlEncoded,
                                method = FormMethod.post
                            ) {
                                id = "form"
                                input {
                                    type = InputType.hidden
                                    id = "routine"
                                    name = "routine"
                                }

                                for (routine in routines) {
                                    section(classes = "container") {

                                        div(classes = "card") {
                                            id = routine.identifier
                                            onClick =
                                                """document.getElementById('routine').value = '${routine.identifier}'; document.getElementById('form').submit();""".trimMargin()
                                            div(classes = "image") {
                                                img(src = routine.icon) {
                                                    alt = ""
                                                }


                                            }
                                            h2 { +routine.name }
                                            p { +routine.description }


                                        }

                                    }

                                }

                            }
                        }

                    }
                }

                //POST [Board routine selection]
                post {
                    val params = call.receiveParameters()
                    params.pq()
                    call.principal<Board>()?.instance?.setBoardRoutine(
                        routinesDb.getIfInDb(params["routine"] ?: "default") ?: return@post
                    )
                    call.respondRedirect("/control")

                }
            }
            authenticate("account") {
                //POST [CODE] Login to session
                route("auth") {
                    get {
                        call.respond(FreeMarkerContent("/controller/controller-auth.ftl", mapOf<String, String>()))
                    }
                    post {
                        val params = call.receiveParameters()

                        val f = StringBuilder()
                        repeat(6) {
                            f.append(params[it.toString()])
                        }
                        val finalizedCode = f.toString().uppercase().pq()
                        if (finalizedCode.length != 6) {
                            call.respondRedirect("/control/auth?reason=length")
//                            call.respond("Enter exactly the 6 digit code")
                            return@post
                        }
                        val board = loginToBoard(finalizedCode, call.principal<Account>())
                        if (board != null) {
                            call.sessions.set(BoardSessionPrinciple(finalizedCode))
                            board.instance.controller = call.principal<Account>()
                            call.respondRedirect("/control")
                        } else
                            call.respondRedirect("/control/auth?reason=notfound")
                    }


                }
            }


        }


    }


}