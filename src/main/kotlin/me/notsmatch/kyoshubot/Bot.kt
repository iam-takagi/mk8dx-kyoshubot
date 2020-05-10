package me.notsmatch.kyoshubot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import me.notsmatch.kyoshubot.command.*
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.MongoService
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*


class Bot (private val token: String) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot

        val mongoService: MongoService = MongoService()
    }

    lateinit var jda: JDA
    val boshuService: BoshuService = BoshuService()

    fun start() {
        instance = this
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build()
        val builder = CommandClientBuilder()

        //builder.setOwnerId("695218967173922866")
        builder.setPrefix(".")

        builder.addCommands(StartCommand(boshuService), EndCommand(boshuService), AddCommand(boshuService), RemoveCommand(boshuService), CanCommand(boshuService), DropCommand(boshuService))
        builder.setHelpWord("kyoshu")

        val client = builder.build()
        jda.addEventListener(Listener())
        jda.addEventListener(client)
    }
}

class Listener : ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        event.jda.guilds.forEach{guild -> println(guild.name)}

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                event.jda.apply {
                    presence.setPresence(OnlineStatus.ONLINE, Activity.watching("type .kyoshu | ${guilds.size} servers"))
                }
            }
        }, 0, 1000*300)
    }
}
