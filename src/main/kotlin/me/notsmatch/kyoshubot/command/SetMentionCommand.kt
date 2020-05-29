package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.util.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import org.apache.commons.lang3.StringUtils
import java.awt.Color

class SetMentionCommand(val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command() {

    init {
        this.name = "setmention"
        this.help = "メンションを変更します\nUsage:\n.setmention everyone\n.setmention here\n.setmention 713167475885473792"
        this.arguments = "<role_id | everyone | here>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)

            if (settings.getCommandOption("setmention") == null || !settings.getCommandOption("setmention")!!.visibility) {
                event.message.delete().complete()
            }

            if(!member.hasPermission(Permission.ADMINISTRATOR)){
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

            val args = StringUtils.split(args)
            if(args.isEmpty()){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("``.setmention <role_id | everyone | here>``")
                }.build())
            }

            if(args[0].equals("everyone", true)){
                settings.apply {
                    mention = "everyone"
                    save()
                    val boshuList = boshuService.getBoshuListByGuildId(guild.idLong) ?: return
                    boshuList.forEach { boshu ->
                        boshu.updateMessage(guild, settings)
                    }
                }
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.CYAN)
                    setAuthor(
                        "メンションを変更しました",
                        null,
                        null
                    )
                    setDescription("メンションはeveryoneになりました")
                }.build())
            }

            else if(args[0].equals("here", true)){
                settings.apply {
                    mention = "here"
                    save()
                    val boshuList = boshuService.getBoshuListByGuildId(guild.idLong) ?: return
                    boshuList.forEach { boshu ->
                        boshu.updateMessage(guild, settings)
                    }
                }
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.CYAN)
                    setAuthor(
                        "メンションを変更しました",
                        null,
                        null
                    )
                    setDescription("メンションはhereになりました")
                }.build())
            }

            else if(!NumberUtils.isLong(args[0])){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("``.setmention <role_id | everyone | here>``")
                }.build())
            } else {

                val roleId = args[0].toLong()

                val role = jda.getRoleById(roleId)
                    ?: return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("そのサーバーには存在しないRoleです")
                    }.build())

                if(!role.isMentionable){
                    return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("ロールのメンションを許可してください")
                    }.build())
                }

                settings.apply {
                    mention = args[0]
                    save()
                    val boshuList = boshuService.getBoshuListByGuildId(guild.idLong) ?: return
                    boshuList.forEach { boshu ->
                        boshu.updateMessage(guild, settings)
                    }
                }
            }
        }
    }
}