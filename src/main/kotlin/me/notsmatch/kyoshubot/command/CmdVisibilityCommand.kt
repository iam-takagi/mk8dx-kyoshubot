package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo
import me.notsmatch.kyoshubot.Bot
import me.notsmatch.kyoshubot.model.CommandOption
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color

class CmdVisibilityCommand (val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command() {

    init {
        this.name = "cv"
        this.help = "コマンドの表示切り替え | trueで表示, falseで非表示。 デフォルト: false"
        this.arguments = "<command> <true|false>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {
            val settings = settingsService.getGuildSettings(guild.idLong)

            if (settings.getCommandOption("cv") == null || !settings.getCommandOption("cv")!!.visibility) {
                event.message.delete().complete()
            }

            val args = StringUtils.split(args)

            if(args.size >= 2) {

                if(!Bot.commands.contains(args[0].toLowerCase())){
                    return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("``.cv <command> <true|false>``\nCommands: " + Bot.commands.contentToString())
                    }.build())
                }

                if (args[1].equals("true", true) || args[1].equals("false", true)) {

                    val option = settings.getCommandOption(args[0])

                    if(option == null) {
                        settings.commandOptions.add(CommandOption(args[0], args[1]!!.toBoolean()))
                    }else{
                        option.visibility = args[1]!!.toBoolean()
                    }

                    settings.save()

                } else {
                    return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("``.cv <command> <true|false>``\nCommands: " + Bot.commands.contentToString())
                    }.build())
                }


            } else {
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("``.cv <command> <true|false>``\nCommands: " + Bot.commands.contentToString())
                }.build())
            }


        }
    }
}