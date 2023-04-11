package me.alexirving.structs.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.alexirving.structs.Routine

@Serializable
@SerialName("board")
class Board(override val identifier: String, override var pwd: String) : User() {
    @Contextual
    val instance = BoardInstance()

    class BoardInstance {
        @Contextual
        var god: Account? = null
        var routine: Routine? = null

    }


}
