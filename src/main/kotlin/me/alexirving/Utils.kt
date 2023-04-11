package me.alexirving


fun randomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}