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


class Bot (private val token: String, val dev: Boolean) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot

        @JvmStatic
        lateinit var mongoService: MongoService

        val commands = arrayOf("add", "c", "cv", "d", "end", "remove", "resend", "setmention", "start", "notify", "tc", "close", "setnotifychannel", "clear")
    }

    lateinit var jda: JDA
    lateinit var boshuService: BoshuService
    lateinit var settingsService: GuildSettingsService
    val eventWaiter = EventWaiter()
    val WEBSITE = System.getenv("WEBSITE")

    fun start() {
        instance = this
        mongoService = MongoService(dev)
        settingsService = GuildSettingsService(mongoService)
        boshuService = BoshuService()
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build()

        val builder = CommandClientBuilder()

        builder.useDefaultGame()

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
            ClearCommand(boshuService, settingsService),
            GuildlistCommand(eventWaiter),
            AboutCommand(Color.GREEN, "https://github.com/riptakagi/KyoshuBot", Permission.VIEW_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)
        )

        builder.setHelpWord("kyoshu")

        val client = builder.build()
        jda.addEventListener(Listener())
        jda.addEventListener(client)
    }
}

class Listener : ListenerAdapter() {

    var p = 0

    override fun onReady(event: ReadyEvent) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                event.jda.apply {
                    if(p == 0) {
                        presence.setPresence(
                            OnlineStatus.ONLINE,
                            Activity.watching("${Bot.instance.WEBSITE} | ${guilds.size} servers")
                        )
                        p = 1
                    }
                    else if(p == 1) {
                        presence.setPresence(
                            OnlineStatus.ONLINE,
                            Activity.playing(".kyoshu - help")
                        )
                        p = 2
                    }
                    else if(p == 2) {
                        presence.setPresence(
                            OnlineStatus.ONLINE,
                            Activity.watching("${Bot.mongoService.boshu_collection.countDocuments()} boshu")
                        )
                        p = 0
                    }
                }
            }
        }, 0, 1000*7)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        event.apply {
            Bot.mongoService.boshu_collection.deleteMany(Filters.eq("guildId", guild.idLong))
            Bot.mongoService.guild_settings_collection.deleteMany(Filters.eq("guildId", guild.idLong))
        }
    }
}
