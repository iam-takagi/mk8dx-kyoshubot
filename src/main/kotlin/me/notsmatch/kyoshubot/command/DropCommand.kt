package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.model.KyoshuUser
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.util.DiscordUtils
import me.notsmatch.kyoshubot.util.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.lang.StringBuilder

class DropCommand(val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command(){

    init {
        this.name = "d"
        this.help = "時間を指定して挙手を下ろします | 管理者専用: .d <hour> <mention>"
        this.arguments = "<hour1> <hour2> <hour3>..."
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)
            if(settings.banned)return reply("This server has been banned.")
            if (settings.getCommandOption("d") == null || !settings.getCommandOption("d")!!.visibility) {
                event.message.delete().complete()
            }

            val boshu = boshuService.getBoshu(guild.idLong, channel.idLong)
                ?: return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("このチャンネルでは募集が開始されていません。")
                }.build())

            val args = StringUtils.split(args)

            if (args.isNotEmpty()) {

                if(args.size == 2 && args[1].startsWith("<@") && args[1].endsWith('>')) {
                    if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                        return replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("You don't have a permission: ADMINISTRATOR")
                        }.build())
                    }

                    var mention = args[1].replace("<", "").replace("@", "").replace(">", "").trim()

                    if (mention.startsWith('!')) {
                        mention = mention.replace("!", "").trim()
                    }

                    println(mention)

                    val other = guild.getMemberById(mention) ?: return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("そのユーザーはサーバー内に存在しません")
                    }.build())

                    if (!NumberUtils.isInteger(args[0]) || args[0].toInt() > 36 || args[0].toInt() < 0) {
                        return replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("hourは0~36で指定する必要があります。")
                        }.build())
                    }

                    val koumoku = boshu.getKoumokuByHour(args[0].toInt()) ?: return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("${args[0]}時の項目は存在しません")
                    }.build())

                    if (koumoku.closed) {
                        return replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("${args[0]}時の項目は強制的に締め切られています")
                        }.build())
                    }

                    if (koumoku.isKyoshu(other.idLong)) {
                        if (koumoku.kyoshuUsers.remove(koumoku.getKyoshuUser(other.idLong))) {

                            boshu.save()

                            boshu.updateMessage(guild, settings, false)
                        }

                    } else {
                        replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("あなたは${args[0]}時に挙手していません")
                        }.build())
                    }
                    return
                }

                args.forEach { arg ->

                    if (!NumberUtils.isInteger(arg) || arg.toInt() > 36 || arg.toInt() < 0) {
                        return replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("hourは0~36で指定する必要があります。")
                        }.build())
                    }

                    val koumoku = boshu.getKoumokuByHour(arg.toInt()) ?: return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("${arg}時の項目は存在しません")
                    }.build())

                    if (koumoku.closed) {
                        return replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("${args[0]}時の項目は強制的に締め切られています")
                        }.build())
                    }

                    if (koumoku.isKyoshu(author.idLong)) {
                        if (koumoku.kyoshuUsers.remove(koumoku.getKyoshuUser(author.idLong))) {

                            boshu.save()

                            boshu.updateMessage(guild, settings, false)
                        }

                    } else {
                        replyInDm(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Error",
                                null,
                                null
                            )
                            setDescription("あなたは${arg}時に挙手していません")
                        }.build())
                    }
                }
            }
        }
    }
}