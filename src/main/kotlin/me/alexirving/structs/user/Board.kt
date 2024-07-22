package me.alexirving.structs.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.alexirving.api.loadBoard

@Serializable
@SerialName("board")
class Board(
    override val identifier: String,
    override var pwd: String) : User() {
    @Transient
    val instance = BoardInstance(code = loadBoard(this))
}
