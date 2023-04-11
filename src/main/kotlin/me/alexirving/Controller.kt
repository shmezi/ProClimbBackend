package me.alexirving

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.li
import kotlinx.html.textInput
import kotlinx.html.ul

//Code: Board  | Board: Code
private val codeBoard = mutableMapOf<String, String>()
private val boardCode = mutableMapOf<String, String>()
fun loadBoard(id: String): String? {

    if (!boardCode.containsKey(id)) {
        val code = randomString(6)
        codeBoard[code] = id
        boardCode[id] = code
        return code
    }


    return null
}

fun getBoard(id: String): String? {
    return codeBoard[id]
}

fun Application.controller() {
    routing {
        authenticate {
            route("control") {

                get {
                    call.respondHtml {
                        body {


                            ul {
                                repeat(6) {
                                    li {
                                        textInput {
                                            name = it.toString()
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


}