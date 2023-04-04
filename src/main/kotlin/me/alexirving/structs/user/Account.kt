package me.alexirving.structs.user

import com.fasterxml.jackson.annotation.JsonTypeName

class Account(identifier: String, pwd: String) : User(identifier, pwd)