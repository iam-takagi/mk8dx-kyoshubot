package me.notsmatch.kyoshubot

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
       Bot(System.getenv("KYOSHUBOT_TOKEN")).start()
    }
}