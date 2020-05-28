package me.notsmatch.kyoshubot.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.lang.StringBuilder

/**
 * @param title タイトル
 * @param hour 1 ~ 24で時間を指定
 * @param need 募集人数
 * @param kyoshuUsers 挙手してるユーザー
 */
data class Koumoku(val title: String, val hour: Int, val need: Int, val kyoshuUsers: MutableList<KyoshuUser>) {

    fun kyoshuSizeText() : String {
        if(getKyoshuSize() >= need){
            return "〆"
        }
        return "@${need-getKyoshuSize()}"
    }

    fun getKyoshuSize() : Int {
        return kyoshuUsers.filter { user -> !user.temporary }.size
    }

    fun getKyoshuUser(id: Long) : KyoshuUser? {
        return kyoshuUsers.find { user -> user.id == id }
    }

    fun isKyoshu(id: Long): Boolean {
        return getKyoshuUser(id) != null
    }

    /**
     * @return JsonObjectにして返します
     */
    fun toJsonObject() : JsonObject {
        val toReturn = JsonObject()
        toReturn.addProperty("title", title)
        toReturn.addProperty("hour", hour)
        toReturn.addProperty("need", need)

        val usersJsonArray = JsonArray()
        kyoshuUsers.forEach{user -> usersJsonArray.add(user.toJsonObject())}

        toReturn.addProperty("kyoshuUsers", usersJsonArray.toString())

        return toReturn
    }
}