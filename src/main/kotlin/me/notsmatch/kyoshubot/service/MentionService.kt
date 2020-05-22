package me.notsmatch.kyoshubot.service

import me.notsmatch.kyoshubot.Bot
import me.notsmatch.kyoshubot.util.DiscordUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import org.bson.Document
import java.awt.Color
import java.lang.StringBuilder

class MentionService(val boshuService: BoshuService) {

    fun getMentionByGuild(guild: Guild) : String {
        val doc = Bot.mongoService.findMentionDocById(guild.idLong)
        if(doc == null){
            return "@everyone"
        } else {
            if(doc.getString("mention").equals("here")){
                return "@here"
            }
            if(doc.getString("mention").equals("everyone")){
                return "@everyone"
            }
            val role = guild.getRoleById(doc.getString("mention"))
            if(role == null){
                setMention(guild.idLong,"everyone")
                return "@everyone"
            }
            if(!role.isMentionable){
                setMention(guild.idLong,"everyone")
                return "@everyone"
            }
            return role.asMention
        }
    }

    fun setMention(guildId: Long, mention: String) {
        Bot.mongoService.replaceMentionDoc(guildId, Document().apply { append("guildId", guildId); append("mention", mention) })
    }

    fun updateMention(guild: Guild) {
        val boshuList = boshuService.getBoshuByGuildId(guild.idLong) ?: return
        boshuList.forEach { boshu ->
            val textChannel = guild.getTextChannelById(boshu.channelId)
            textChannel?.editMessageById(boshu.messageId, EmbedBuilder().apply {
                setColor(Color.CYAN)
                setAuthor(
                    "募集が進行中です",
                    null,
                    null
                )
                val builder =
                    StringBuilder("${getMentionByGuild(guild)}\nタイトル: " + boshu.title + "\n" + ".add <hour> <need> <title> を使用して挙手項目を追加してください。")
                builder.append("==========================\n")
                val it = boshu.koumokuList.iterator()
                while (it.hasNext()) {
                    val k = it.next()
                    val b =
                        StringBuilder("・${k.hour}時 @${k.need - k.kyoshuUsers.size} ${k.title}")
                    if (k.kyoshuUsers.size >= 1) {
                        b.append("\n")
                        k.kyoshuUsers.forEach { id ->
                            val member = guild.getMemberById(id)
                            if (member != null) {
                                b.append(DiscordUtils.getName(member))
                            }
                        }
                    }
                    builder.append(b.toString())
                    if (it.hasNext()) {
                        builder.append("\n")
                    }
                }
                setDescription(builder.toString())
            }.build())?.queue()
        }
    }
}