package me.notsmatch.kyoshubot.service

import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document


class MongoService {

    val client: MongoClient
    val database: MongoDatabase
    val boshu_collection: MongoCollection<Document>

    init {
        val serverAddress = ServerAddress(System.getenv("mongo.host"), System.getenv("mongo.port").toInt())

        if (System.getenv("mongo.authentication.enabled").toBoolean()) {
            val credential = MongoCredential.createCredential(
                System.getenv("mongo.authentication.username"),
                System.getenv("mongo.authentication.database"),
                System.getenv("mongo.authentication.password").toCharArray()
            )

            this.client = MongoClient(
                serverAddress ,
                listOf<MongoCredential>(credential)
            )
        } else {
            this.client = MongoClient(serverAddress)
        }

        this.database = this.client.getDatabase(System.getenv("mongo.database"))
        this.boshu_collection = this.database.getCollection(System.getenv("mongo.collections.boshu"))
    }

    /**
     * @param guildId
     * @param channelId
     * @return 募集ドキュメントを返します
     */
    fun findBoshuById(guildId: Long, channelId: Long): Document? {
        return this.boshu_collection.find(Filters.and(Filters.eq("guildId", guildId), Filters.eq("channelId", channelId))).first() ?: return null
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
}