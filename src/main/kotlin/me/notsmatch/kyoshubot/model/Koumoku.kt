package me.notsmatch.kyoshubot.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.notsmatch.kyoshubot.Bot
import net.dv8tion.jda.api.entities.Guild
import java.lang.StringBuilder

/**
 * @param title タイトル
 * @param hour 1 ~ 24で時間を指定
 * @param need 募集人数
 * @param kyoshuUsers 挙手してるユーザー
 */
data class Koumoku(val title: String, val hour: Int, val need: Int, var closed: Boolean, val kyoshuUsers: MutableList<KyoshuUser>) {


    fun kyoshuSizeText() : String {
        if(isClosed()){
            return "〆"
        }
        return "@${getKyoshuLeft()}"
    }

    /**
     * 満員までの残りを返します
     */
    fun getKyoshuLeft() : Int{
       return need-getKyoshuSize()
    }

    /**
     * 挙手人数を返します
     */
    fun getKyoshuSize() : Int {
        return kyoshuUsers.size
    }

    /**
     * 挙手ユーザーを返します
     */
    fun getKyoshuUser(id: Long) : KyoshuUser? {
        return kyoshuUsers.find { user -> user.id == id }
    }

    /**
     *
     */
    fun isKyoshu(id: Long): Boolean {
        return getKyoshuUser(id) != null
    }

    fun isClosed() : Boolean {
        if(closed) {
            return true
        }
        return getKyoshuSize() >= need
    }

    /**
     * @return JsonObjectにして返します
     */
    fun toJsonObject() : JsonObject {
        val toReturn = JsonObject()
        toReturn.addProperty("title", title)
        toReturn.addProperty("hour", hour)
        toReturn.addProperty("need", need)
        toReturn.addProperty("closed", closed)

        val usersJsonArray = JsonArray()
        kyoshuUsers.forEach{user -> usersJsonArray.add(user.toJsonObject())}

        toReturn.addProperty("kyoshuUsers", usersJsonArray.toString())

        return toReturn
    }

    fun getKyoshuUsersMention(guild: Guild): String {
        val builder = StringBuilder()

        kyoshuUsers.forEach { user ->
            builder.append(guild.getMemberById(user.id)!!.asMention)
            if(user.temporary){
                builder.append("(仮)")
            }
            builder.append(" ")
        }

        return builder.toString()
    }
}