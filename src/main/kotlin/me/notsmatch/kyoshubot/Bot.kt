package me.notsmatch.kyoshubot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.mongodb.client.model.Filters
import me.notsmatch.kyoshubot.command.*
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.service.MongoService
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.util.*


class Bot (private val token: String) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot

        @JvmStatic
        val mongoService: MongoService = MongoService()

        val commands = arrayOf("add", "c", "cv", "d", "end", "remove", "resend", "setmention", "start", "notify", "tc", "close", "setnotifychannel")
    }

    lateinit var jda: JDA
    val boshuService: BoshuService = BoshuService()
    val settingsService: GuildSettingsService = GuildSettingsService(mongoService, boshuService)
    val eventWaiter = EventWaiter()

    fun start() {
        instance = this
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build()

        val builder = CommandClientBuilder()

        builder.setOwnerId("695218967173922866")
        builder.setPrefix(".")

        builder.addCommands(
            StartCommand(boshuService, settingsService),
            EndCommand(boshuService, settingsService),
            AddCommand(boshuService, settingsService),
            RemoveCommand(boshuService, settingsService),
            CanCommand(boshuService, settingsService),
            TemporaryCanCommand(boshuService, settingsService),
            DropCommand(boshuService, settingsService),
            ResendCommand(boshuService, settingsService),
            NotifyCommand(boshuService, settingsService),
            CmdVisibilityCommand(boshuService, settingsService),
            SetMentionCommand(boshuService, settingsService),
            CloseCommand(boshuService, settingsService),
            OpenCommand(boshuService, settingsService),
            SetNotifyChannelCommand(boshuService, settingsService),
            ReminderCommand(boshuService, settingsService),
            GuildlistCommand(eventWaiter),
            AboutCommand(Color.GREEN, "https://github.com/notsmatch/KyoshuBot", Permission.VIEW_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION)
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
                    presence.setPresence(OnlineStatus.ONLINE, Activity.watching(".kyoshuabout | ${guilds.size} servers"))
                }
            }
        }, 0, 1000*300)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        event.apply {
            Bot.mongoService.boshu_collection.deleteMany(Filters.eq("guildId", guild.idLong))
            Bot.mongoService.guild_settings_collection.deleteMany(Filters.eq("guildId", guild.idLong))
        }
    }
}
