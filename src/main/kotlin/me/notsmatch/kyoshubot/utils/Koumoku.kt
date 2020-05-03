package me.notsmatch.kyoshubot.utils

import net.dv8tion.jda.api.entities.User

/**
 * @param title タイトル
 * @param time 1 ~ 24で時間を指定
 * @param need 募集人数
 * @param kyoshuUsers 挙手してるユーザー
 */
data class Koumoku(val title: String, val hour: Int, val need: Int, val kyoshuUsers: MutableList<Long>)