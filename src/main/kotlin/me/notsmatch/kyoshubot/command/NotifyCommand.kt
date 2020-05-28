package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class NotifyCommand : Command() {

    init {
        this.name = "n"
        this.help = "時間に挙手しているユーザーに"
        this.arguments = "<hour> <message> "
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

        }
    }
}