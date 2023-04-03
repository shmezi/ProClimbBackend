package me.alexirving.structs

import io.ktor.server.auth.*

data class RawCookie(val id: String, val cookie: String) : Principal