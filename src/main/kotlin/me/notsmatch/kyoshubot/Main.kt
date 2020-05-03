package me.notsmatch.kyoshubot

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val bot = Bot(Secret.TOKEN)
        bot.start()
    }
}