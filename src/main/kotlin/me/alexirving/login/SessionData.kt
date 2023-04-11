package me.alexirving.login

import kotlinx.serialization.Serializable

@Serializable

data class SessionData(
    val date: String, //TODO: Switch to a DATE object and make it serializable.
    val ip: String
)