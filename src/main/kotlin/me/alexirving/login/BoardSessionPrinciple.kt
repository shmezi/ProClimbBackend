package me.alexirving.login

import io.ktor.server.auth.*

data class BoardSessionPrinciple(val code: String) : Principal