package me.alexirving.structs.user

import kotlinx.serialization.Serializable
import me.alexirving.api.killUserSession
import me.alexirving.api.loadBoard
import me.alexirving.structs.Routine

@Serializable
data class BoardInstance(
    var controller: Account? = null,
    var routine: Routine? = null,
    var code: String?
) {
    private var connected = false;
    var runCommand: ((String) -> Unit)? = null

    fun connect(user: Account?) {
        this.controller = user
        connected = true
        code = null
        runCommand?.invoke("CONNECTION ${controller?.identifier}")
    }

    fun setBoardRoutine(routine: Routine) {
        this.routine = routine
        this.routine?.apply {
            runCommand?.invoke("ROUTINE ${controller?.identifier} $identifier $hangTime $pauseTime $roundCount $restTime $numberOfSets");
        }
    }

    fun disconnect(board: Board): String? {
        if (code == null) {
            routine = null
            controller = null
            connected = false
            killUserSession(board)
            code = loadBoard(board)
        }
        return code
    }
}