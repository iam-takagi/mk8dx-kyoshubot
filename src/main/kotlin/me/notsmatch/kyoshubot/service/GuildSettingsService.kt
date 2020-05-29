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
}