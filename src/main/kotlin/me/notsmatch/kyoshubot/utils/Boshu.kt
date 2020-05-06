package me.notsmatch.kyoshubot.utils

class Boshu(val guildId: Long, val channelId: Long, val title: String, var koumokuList: MutableList<Koumoku>) {

    var messageId: Long = 0

    fun getKoumokuByHour(hour: Int) : Koumoku? {
        val a  = koumokuList.stream().filter { it.hour == hour }.findFirst()
        if(a.isPresent)return a.get()
        return null
    }
}