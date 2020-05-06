package me.notsmatch.kyoshubot

import me.notsmatch.kyoshubot.secret.Secret

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val bot = Bot(Secret.TOKEN)
        bot.start()
    }
}