package me.notsmatch.kyoshubot.service

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import me.notsmatch.kyoshubot.Bot
import org.bson.Document
import org.bson.conversions.Bson


class MongoService {

    val client: MongoClient
    val database: MongoDatabase
    val boshu_collection: MongoCollection<Document>
    val guild_settings_collection: MongoCollection<Document>


    init {

        this.client = MongoClient(MongoClientURI(System.getenv("MONGO_URI")))
        this.database = this.client.getDatabase(System.getenv("MONGO_DATABASE"))

        this.boshu_collection = this.database.getCollection("boshu")
        this.guild_settings_collection = this.database.getCollection("settings")
    }

    /**
     * @param guildId
     * @param channelId
     * @return 募集ドキュメントを返します
     */
    fun findBoshuByGuildAndChannel(guildId: Long, channelId: Long): Document? {
        return this.boshu_collection.find(Filters.and(Filters.eq("guildId", guildId), Filters.eq("channelId", channelId))).first() ?: return null
    }

    /**
     * @param guildId
     * @return 全ての募集ドキュメントを返します
     */
    fun findBoshuByGuild(guildId: Long): FindIterable<Document>? {
        return this.boshu_collection.find(Filters.eq("guildId", guildId)) ?: return null
    }

    /**
     * @param guildId
     */
    fun findGuildSettingsDocById(guildId: Long): Document? {
        return this.guild_settings_collection.find(Filters.and(Filters.eq("guildId", guildId))).first() ?: return null
    }

    /**
     * @param guildId
     * @param channelId
     * @return 募集ドキュメントを置き換えます
     */
    fun replaceBoshu(guildId: Long, channelId: Long, document: Document) {
        this.boshu_collection.replaceOne(
            Filters.and(Filters.eq("guildId", guildId), Filters.eq("channelId", channelId)), document,
            ReplaceOptions().upsert(true)
        )
    }

    fun replaceGuildSettings(guildId: Long, document: Document) {
        this.guild_settings_collection.replaceOne(
            Filters.and(Filters.eq("guildId", guildId)), document,
            ReplaceOptions().upsert(true)
        )
    }
}