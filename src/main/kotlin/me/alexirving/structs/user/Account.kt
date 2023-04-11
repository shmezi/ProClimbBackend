package me.alexirving.structs.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.alexirving.structs.UserLog

@Serializable
@SerialName("account")
class Account(
    override val identifier: String,
    override var pwd: String,
    val logs: MutableList<UserLog>
) : User()
