package me.notsmatch.kyoshubot.model

import com.google.gson.JsonObject

data class KyoshuUser(val id: Long, val temporary: Boolean) {

    fun toJsonObject() : JsonObject {
        val toReturn = JsonObject()
        toReturn.addProperty("id", id)
        toReturn.addProperty("temporary", temporary)
        return toReturn
    }
}