package me.alexirving.login

import io.ktor.server.auth.*

data class RawCookie(val id: String, var cookie: String) : Principal