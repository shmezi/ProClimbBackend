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
import me.alexirving.login.BoardSessionPrinciple
import me.alexirving.randomString
import me.alexirving.routines
import me.alexirving.structs.user.Account
import me.alexirving.structs.user.Board
import kotlin.collections.set

//Code: Board  | Board: Code
private val codeBoard = mutableMapOf<String, Board>()
private val boardCode = mutableMapOf<Board, String>()

//Code: UserID
private val inUse = mutableMapOf<String, Board>()
fun loadBoard(board: Board): String {
    val code = randomString(6)
    codeBoard[code] = board
    boardCode[board] = code
    return code

}

fun loginToBoard(code: String, user: Account?): Board? {
    val board = codeBoard[code] ?: return null
    inUse[code] = board
    codeBoard.remove(code)
    board.instance.connect(user)
    return board
}

fun validateUser(code: String): Board? {
    return inUse[code]
}

fun killUserSession(board: Board) = inUse.remove(boardCode[board])

fun getCode(board: Board) = boardCode[board]


fun Application.controller() {
    routing {
        route("control") {
            authenticate("session", "account", strategy = AuthenticationStrategy.Required) {
                get {
                    call.respondHtml {

                        body {
                            p {
                                +"Connected to board: ${call.principal<Board>()?.identifier}"
                            }
                            h3 {
                                +"Select a routine:"
                            }
                            form(
                                action = "/control",
                                encType = FormEncType.applicationXWwwFormUrlEncoded,
                                method = FormMethod.post
                            ) {
                                select {
                                    name = "routine"
                                    option {
                                        value = "default"
                                        +"Normal"
                                    }
                                    option {
                                        value = "fast"
                                        +"Speed"
                                    }
                                }
                                postButton { +"Select!" }
                            }

                        }
                    }
                }
                post {
                    val params = call.receiveParameters()
                    call.principal<Board>()?.instance?.setBoardRoutine(
                        routines.getIfInDb(params["routine"] ?: "default") ?: return@post
                    )
                    call.respondRedirect("/control")
                }
            }
            authenticate("account") {
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
                        val finalizedCode = f.toString().uppercase()
                        if (finalizedCode.length != 6) {
                            call.respond("Enter exactly the 6 digit code")
                            return@post
                        }
                        val board = loginToBoard(finalizedCode, call.principal<Account>())
                        if (board != null) {
                            call.sessions.set(BoardSessionPrinciple(finalizedCode))
                            board.instance.controller = call.principal<Account>()
                            call.respondRedirect("/control")
                        } else
                            call.respond("Could not find the board with id of: $finalizedCode. Is it on?")
                    }


                }
            }


        }


    }


}