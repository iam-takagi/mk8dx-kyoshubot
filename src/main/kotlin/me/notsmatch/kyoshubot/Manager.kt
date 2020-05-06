package me.notsmatch.kyoshubot

import me.notsmatch.kyoshubot.model.Boshu

object Manager {

    val boshuList: MutableList<Boshu> = mutableListOf()

    /**
     * @param guildId
     * @param channelId]
     * @return 募集を返します
     */
    fun getBoshu(guildId: Long, channelId: Long) : Boshu? {
       val a  = boshuList.stream().filter { it.guildId == guildId && it.channelId == channelId }.findFirst()
        if(a.isPresent)return a.get()
        return null
    }

    /**
     * 募集を追加します
     * @param guildId
     * @param channelId
     * @return 該当のギルドのチャンネルで既に募集中の場合はfalseを返します
     */
    fun addBoshu(guildId: Long, channelId: Long, title: String) : Boolean {
        if (getBoshu(guildId, channelId) != null) return false
        boshuList.add(Boshu(guildId, channelId, title, mutableListOf()))
        return true
    }

    /**
     * 募集を削除します
     * @param guildId
     * @param channelId
     * @return 削除結果を返します
     */
    fun removeBoshu(guildId: Long, channelId: Long) : Boolean{
        val boshu = getBoshu(guildId, channelId)
        if(boshu === null)return false
        return boshuList.remove(boshu)
    }
}