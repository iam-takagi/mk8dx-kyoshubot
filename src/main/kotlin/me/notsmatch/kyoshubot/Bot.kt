package me.notsmatch.kyoshubot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import me.notsmatch.kyoshubot.commands.*
import me.notsmatch.kyoshubot.utils.Manager
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*
import javax.annotation.Nonnull

class Bot (private val token: String) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot

        @JvmStatic
        val random: Random = Random()
    }

    lateinit var jda: JDA

    fun start() {
        instance = this
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build()
        val builder = CommandClientBuilder()

        builder.setOwnerId("695218967173922866")
        builder.setPrefix("!")

        builder.addCommands(StartCommand(), EndCommand(), AddCommand(), RemoveCommand(), CanCommand(), DropCommand())

        builder.setHelpWord("kyoshu")
        val client = builder.build()
        jda.addEventListener(client)
        jda.addEventListener(Listener())
    }
}

class Listener : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(event.message.contentRaw.startsWith("!")){
            event.message.delete().complete()
        }
    }
}
