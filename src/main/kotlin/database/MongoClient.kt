package database

import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import utils.Properties

object MongoClient {

    private val client = MongoClient(
        Properties.get("mongo.host"),
        Properties.get("mongo.port").toInt()
    )

    private val codecs = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    private val database = client.getDatabase(Properties.get("mongo.database"))
        .withCodecRegistry(codecs)

    fun <T> insert(collection: String, entry: T, entryType: Class<T>) {
        database.getCollection(collection, entryType).insertOne(entry)
    }

    fun <T> read(collection: String, entryType: Class<T>): List<T> {
        return database.getCollection(collection, entryType).find().toList()
    }
}