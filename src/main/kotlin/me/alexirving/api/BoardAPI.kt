package me.alexirving.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import me.alexirving.lib.util.pq
import me.alexirving.structs.user.Board
import me.alexirving.users


fun Application.api() {

    routing {

        authenticate("board-api") {
            route("api") {
                webSocket("/connection") {
                    val board = call.principal<Board>() ?: return@webSocket
                    val user = board.instance
                    val id = board.identifier
                    fun sendCommand(message: String) {
                        runBlocking {
                            send(Frame.Text(message))
                        }
                        message.pq("$id(Server->Board)")
                    }
                    user.runCommand = ::sendCommand
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val raw: List<String> = frame.readText().uppercase().split(' ')
                        val command = raw[0]
                        when (command.pq("$id(Server<-Board)")) {
                            "CODE" -> {
                                sendCommand("CODE ${user.code}")
                            }
                            "DISCONNECT" -> {
                                sendCommand("CODE ${user.disconnect(board)}")
                            }
                            "SUCCESS" -> {
                                val userId = user.routine?.identifier ?: return@webSocket
                                users.update(userId)
                                user.controller?.log(userId, true)
                            }
                        }

                    }

//                    close()
                }

            }

        }
    }


}