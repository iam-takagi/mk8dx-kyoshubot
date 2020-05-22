package me.notsmatch.kyoshubot.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import me.notsmatch.kyoshubot.Bot
import net.dv8tion.jda.api.entities.Guild
import org.bson.Document

class MentionService {

    fun getMentionByGuildId(guildId: Long, guild: Guild) : String {
        val doc = Bot.mongoService.findMentionDocById(guildId)
        if(doc == null){
            return "@everyone"
        } else {
            if(doc.getString("mention").equals("here")){
                return "@here"
            }
            val role = guild.getRoleById(doc.getString("mention"))
            if(role == null){
                setMention(guildId,"everyone")
                return "@everyone"
            }
            if(!role.isMentionable){
                setMention(guildId,"everyone")
                return "@everyone"
            }
            return role.asMention
        }
    }

    fun setMention(guildId: Long, mention: String) {
        Bot.mongoService.replaceMentionDoc(guildId, Document().apply { append("guildId", guildId); append("mention", mention) })
    }
}