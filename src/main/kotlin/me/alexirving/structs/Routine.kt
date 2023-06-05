package me.alexirving.structs

import kotlinx.serialization.Serializable
import me.alexirving.lib.database.core.Cacheable

@Serializable
class Routine(
    override val identifier: String,
    var name: String,
    var icon: String,
    var description: String,
    var hangTime: Int,
    var pauseTime: Int,
    var roundCount: Int,
    var restTime: Int,
    var numberOfSets: Int
) : Cacheable<String>