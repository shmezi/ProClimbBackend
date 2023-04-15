package me.alexirving.structs.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.alexirving.structs.UserLog
import java.time.Instant
import java.util.*

@Serializable
@SerialName("account")
class Account(
    override val identifier: String,
    override var pwd: String,
    val logs: MutableList<UserLog>
) : User() {
    fun log(routineId: String, success: Boolean = true) {
        logs.add(UserLog(routineId, Date.from(Instant.now()).toString(), success))
    }
}
