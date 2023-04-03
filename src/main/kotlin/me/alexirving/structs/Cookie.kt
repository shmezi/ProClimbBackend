package me.alexirving.structs

import io.ktor.server.auth.*
import me.alexirving.lib.database.core.Cacheable

class Cookie(id: String, val cookies: MutableList<String>) : Principal, Cacheable<String>(id)