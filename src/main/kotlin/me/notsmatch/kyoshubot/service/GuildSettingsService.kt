package me.notsmatch.kyoshubot.service

import me.notsmatch.kyoshubot.model.GuildSettings
import me.notsmatch.kyoshubot.util.DiscordUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import java.awt.Color
import java.lang.StringBuilder

class GuildSettingsService(val mongoService: MongoService, val boshuService: BoshuService) {

    fun getGuildSettings(guildId: Long) : GuildSettings {
        mongoService.apply {
            val doc = findGuildSettingsDocById(guildId)
            if(doc == null){
                val settings = GuildSettings(guildId, "everyone", mutableListOf())
                replaceGuildSettings(guildId, settings.toDocument())
                return settings
            }
            return GuildSettings.fromDocument(doc)
        }
    }

    fun updateMention(guild: Guild, settings: GuildSettings) {
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
                    StringBuilder("${settings.getMentionString(guild)}\nタイトル: " + boshu.title + "\n" + ".add <hour> <need> <title> を使用して挙手項目を追加してください。")
                builder.append("==========================\n")
                val it = boshu.koumokuList.iterator()
                while (it.hasNext()) {
                    val k = it.next()
                    val b =
                        StringBuilder("・${k.hour}時 @${k.need - k.getKyoshuSize()} ${k.title}")
                    if (k.kyoshuUsers.size >= 1) {
                        b.append("\n")
                        k.kyoshuUsers.forEach { user ->
                            val member = guild.getMemberById(user.id)
                            if (member != null) {
                                b.append(DiscordUtils.getName(member))
                                if(user.temporary){
                                    b.append("(仮)")
                                }
                                b.append(" ")
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