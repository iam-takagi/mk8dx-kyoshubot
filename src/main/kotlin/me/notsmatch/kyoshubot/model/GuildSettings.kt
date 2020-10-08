package me.notsmatch.kyoshubot.model

import com.google.gson.JsonObject
import com.mongodb.BasicDBList
import me.notsmatch.kyoshubot.Bot
import me.notsmatch.kyoshubot.util.JsonUtils
import net.dv8tion.jda.api.entities.Guild
import org.bson.Document

data class CommandOption(val command: String, var visibility: Boolean) {

    fun toJsonObject() : JsonObject {
        val toReturn = JsonObject()
        toReturn.addProperty("command", command)
        toReturn.addProperty("visibility", visibility)

        return toReturn
    }
}

data class GuildSettings(val guildId: Long, var mention: String?, var notifyChannelId: Long, val banned: Boolean, val commandOptions: MutableList<CommandOption>) {

    fun toDocument() : Document {
        return Document().apply {
            put("guildId", guildId)
            put("mention", mention)
            put("notifyChannelId", notifyChannelId)
            put("banned", banned)
            val commandOptionsArray = BasicDBList()

            commandOptions.forEach { option ->
                commandOptionsArray.add(option.toJsonObject().toString())
            }

            put("commandOptions", commandOptionsArray)
        }
    }

    fun save() {
        Bot.mongoService.replaceGuildSettings(guildId, toDocument())
    }

    fun getCommandOption(command: String) : CommandOption?{
        return commandOptions.find { option -> option.command == command }
    }

    fun getMentionString(guild: Guild) : String {
        if(mention == null){
            return "(@everyone)"
        } else {
            if(mention.equals("none")) {
                return ""
            }
            if(mention.equals("here")){
                return "(@here)"
            }
            if(mention.equals("everyone")){
                return "(@everyone)"
            }
            val role = guild.getRoleById(mention!!)
            if(role == null || !role.isMentionable){
                this.mention = "everyone"
                return "(@everyone)"
            }
            return "(" + role.asMention + ")"
        }
    }

    companion object {

        fun fromDocument(document: Document) : GuildSettings {
            document.apply {
                //格納用
                val commandOptions = mutableListOf<CommandOption>()

                val docClazz: Class<out MutableList<*>?> = ArrayList<String>().javaClass
                val commandOptionsArray = get("commandOptions", docClazz)!!

                //取り出し
                commandOptionsArray.forEach { str ->
                    val commandOptionJson = JsonUtils.JSON_PARSER.parse(str.toString()).asJsonObject
                    commandOptions.add(CommandOption(commandOptionJson.get("command").asString, commandOptionJson.get("visibility").asBoolean))
                }

                return GuildSettings(getLong("guildId"), getString("mention"), getLong("notifyChannelId"), getBoolean("banned"), commandOptions)
            }
        }
    }
}