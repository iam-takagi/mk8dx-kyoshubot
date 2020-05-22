package me.notsmatch.kyoshubot.util

import net.dv8tion.jda.api.entities.Member

object DiscordUtils {

    fun getName(member: Member) : String{
        if(member.nickname != null){
            return member.nickname!!
        }
        return member.user.name
    }

}

