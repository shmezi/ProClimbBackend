package me.alexirving.login

import io.ktor.server.auth.*

data class CookiePrincipal(val id: String, var cookie: String) : Principal