package me.notsmatch.kyoshubot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.mongodb.client.model.Filters
import me.notsmatch.kyoshubot.command.*
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.MentionService
import me.notsmatch.kyoshubot.service.MongoService
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*


class Bot (private val token: String) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot

        val mongoService: MongoService = MongoService()

        val dev = true
    }

    lateinit var jda: JDA
    val boshuService: BoshuService = BoshuService()
    val mentionService: MentionService = MentionService(boshuService)

    fun start() {
        instance = this
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build()
        val builder = CommandClientBuilder()

        builder.setOwnerId("695218967173922866")
        builder.setPrefix(".")

        builder.addCommands(
            StartCommand(boshuService, mentionService),
            EndCommand(boshuService),
            AddCommand(boshuService, mentionService),
            RemoveCommand(boshuService, mentionService),
            CanCommand(boshuService, mentionService),
            DropCommand(boshuService, mentionService),
            SetMentionCommand(mentionService)
        )

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

    override fun onGuildLeave(event: GuildLeaveEvent) {
        event.apply {
            Bot.mongoService.boshu_collection.deleteMany(Filters.eq("guildId", guild.idLong))
            Bot.mongoService.mention_collection.deleteMany(Filters.eq("guildId", guild.idLong))
        }
    }
}
