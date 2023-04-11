package me.alexirving.structs

import me.alexirving.lib.database.core.Cacheable

class Routine(
    override val identifier: String,
    val startInterval: Int,
    val hangTime: Int,
    val interval: Int
) : Cacheable<String>