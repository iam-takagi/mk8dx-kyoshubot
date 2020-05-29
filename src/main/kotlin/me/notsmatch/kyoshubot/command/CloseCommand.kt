package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import org.apache.commons.lang3.StringUtils

class CloseCommand (val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command() {

    init {
        this.name = "close"
        this.help = "時間を指定して項目を締め切ります"
        this.arguments = "<hour1> <hour2> <hour3>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {
            val settings = settingsService.getGuildSettings(guild.idLong)

            if (settings.getCommandOption("close") == null || !settings.getCommandOption("close")!!.visibility) {
                event.message.delete().complete()
            }

            val args = StringUtils.split(args)
        }
    }
}