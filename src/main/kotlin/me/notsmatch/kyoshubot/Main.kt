package me.notsmatch.kyoshubot

import io.github.cdimascio.dotenv.dotenv

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Bot(System.getenv("TOKEN")).start()
    }
}