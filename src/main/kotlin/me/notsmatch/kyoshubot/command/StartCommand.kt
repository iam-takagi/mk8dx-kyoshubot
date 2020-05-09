package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class StartCommand(val boshuService: BoshuService) : Command(){

    init {
        this.name = "start"
        this.help = "募集を開始します"
        this.arguments = "<title>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            event.message.delete().complete()

            val args = args.split(" ")
            val title = args[0]
            if(title.isEmpty()){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: タイトルを入力してください")
                }.build())
            }

            if(title.length > 30){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: タイトルは30文字以下に設定してください")
                }.build())
            }
            if (boshuService.addBoshu(guild.idLong, channel.idLong, title)) {
                val boshu = boshuService.getBoshu(guild.idLong, channel.idLong)!!
                boshu.messageId = channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.CYAN)
                        setAuthor(
                            "募集を開始しました",
                            null,
                            null
                        )
                        setDescription("@everyone\nタイトル: " + title + "\n" + ".add <hour> <need> <title> を使用して挙手項目を追加してください。")
                    }.build()
                ).complete().idLong
                boshu.save()
            } else {
                replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: このチャンネルでは既に募集が開始されています。 募集を終了するには !end を使用してください。")
                }.build())
            }
        }
    }
}