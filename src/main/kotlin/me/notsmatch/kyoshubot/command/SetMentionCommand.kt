package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService

class SetMentionCommand(val boshuService: BoshuService) : Command() {

    init {
        this.name = "setmention"
        this.help = "メンションを変更します (デフォルト: everyone)"
        this.arguments = "<ロール名>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

        }
    }
}