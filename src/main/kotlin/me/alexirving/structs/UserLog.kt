package me.alexirving.structs

import kotlinx.serialization.Serializable
import java.util.*
@Serializable
data class UserLog(
    val routineId: String,
    val date: String,
    val success: Boolean
)