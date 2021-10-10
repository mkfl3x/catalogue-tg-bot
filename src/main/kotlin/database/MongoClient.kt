package database

import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

class MongoClient(
    private val dbName: String,
    private val host: String,
    private val port: Int
) {

    private val codecs = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    private val client = MongoClient(host, port)

    private val db = client.getDatabase(dbName)
        .withCodecRegistry(codecs)

    fun <T> insert(collection: String, entry: T, entryType: Class<T>) {
        db.getCollection(collection, entryType).insertOne(entry)
    }

    fun <T> read(collection: String, entryType: Class<T>): List<T> {
        return db.getCollection(collection, entryType).find().toList()
    }
}