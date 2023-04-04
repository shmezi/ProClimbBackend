package me.alexirving.structs.user

import com.fasterxml.jackson.annotation.JsonTypeName
class Board(identifier: String, pwd: String) : User(identifier, pwd) {
}