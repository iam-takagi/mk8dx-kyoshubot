package me.notsmatch.kyoshubot.model

import com.google.gson.JsonElement
import com.mongodb.BasicDBList
import me.notsmatch.kyoshubot.Bot
import me.notsmatch.kyoshubot.Config
import me.notsmatch.kyoshubot.util.JsonUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import org.bson.Document
import java.lang.StringBuilder

data class Boshu(val guildId: Long, val channelId: Long, val title: String, var messageId: Long, var koumokuList: MutableList<Koumoku>) {

    /**
     * @return 時間から項目を返します
     */
    fun getKoumokuByHour(hour: Int) : Koumoku? {
        val a  = koumokuList.stream().filter { it.hour == hour }.findFirst()
        if(a.isPresent)return a.get()
        return null
    }

    /**
     * @return ドキュメントにして返します
     */
    fun toDocument() : Document {
        return Document().apply {
            put("title", title)
            put("channelId", channelId)
            put("guildId", guildId)
            put("messageId", messageId)

            val koumokuArray = BasicDBList()

            koumokuList.forEach { koumoku ->
                koumokuArray.add(koumoku.toJsonObject().toString())
            }

            put("koumoku", koumokuArray)
        }
    }

    /**
     * 保存します
     */
    fun save() {
        Bot.mongoService.replaceBoshu(guildId, channelId, toDocument())
    }

    fun toEmbed(guild: Guild, settings: GuildSettings, end: Boolean) : MessageEmbed {
        return EmbedBuilder().apply {
            setColor(Config.EMBED_COLOR)
            if (!end) {
                setAuthor(
                    "募集が進行中です",
                    null,
                    null
                )
            } else {
                setAuthor(
                    "募集は終了しました",
                    null,
                    null
                )
            }

            setTitle(title)

            val sb =
                StringBuilder("`.add <hour> <need> <title> - 挙手項目を追加`")
            sb.append("\n\n")
            val it = koumokuList.iterator()

            while (it.hasNext()) {
                val k = it.next()
                val b = StringBuilder("> ${k.hour}時 ${k.kyoshuSizeText()} ${k.title}")
                if (k.kyoshuUsers.size >= 1) {
                    b.append("\n> ")
                    k.kyoshuUsers.forEach { user ->
                        val member = guild.getMemberById(user.id)
                        if (member != null) {
                            b.append(member.effectiveName)
                            if (user.temporary) {
                                b.append("(仮)")
                            }
                            b.append(" ")
                        }
                    }
                }
                sb.append(b.toString())
                if (it.hasNext()) {
                    sb.append("\n\n")
                }
            }
            setDescription(sb.toString())
        }.build()
    }

    fun getTextChannel(guild: Guild) : TextChannel?{
        return guild.getTextChannelById(channelId) ?: return null
    }

    /**
     * メッセージ更新
     */
    fun updateMessage(guild: Guild, settings: GuildSettings, end: Boolean) {
        getTextChannel(guild)!!.editMessageById(messageId, toEmbed(guild, settings, end)).queue()
    }

    companion object {

        /**
         * @return Documentから募集オブジェクトにして返します
         */

        fun fromDocument(document: Document) : Boshu {
            document.apply {

                //格納用
                val koumokuList = mutableListOf<Koumoku>()

                val docClazz: Class<out MutableList<*>?> = ArrayList<String>().javaClass
                val koumokuArray = get("koumoku", docClazz)!!

                //各項目取り出し koumoku:
                koumokuArray.forEach{str ->

                    //格納用
                    val kyoshuUsers = mutableListOf<KyoshuUser>()

                    val koumokuJson = JsonUtils.JSON_PARSER.parse(str.toString()).asJsonObject
                    val kyoshuUsersArray = JsonUtils.JSON_PARSER.parse(koumokuJson.get("kyoshuUsers").asString).asJsonArray

                    //挙手ユーザーリストに追加
                    kyoshuUsersArray.forEach{
                        t: JsonElement -> val json = t.asJsonObject
                        kyoshuUsers.add(KyoshuUser(json.get("id").asLong, json.get("temporary").asBoolean))
                    }

                    //項目リストに追加
                    koumokuList.add(
                        Koumoku(
                            koumokuJson.get("title").asString,
                            koumokuJson.get("hour").asInt,
                            koumokuJson.get("need").asInt,
                            koumokuJson.get("closed").asBoolean,
                            kyoshuUsers
                        )
                    )
                }

                //募集オブジェクトにしてreturn
                return Boshu(
                    document.getLong("guildId"),  document.getLong("channelId"), document.getString("title"), document.getLong("messageId"), koumokuList
                )
            }
        }
    }
}