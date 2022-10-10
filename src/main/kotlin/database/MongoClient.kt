package database

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import com.mongodb.MongoClientURI
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.conversions.Bson
import utils.Properties

object MongoClient {

    private val connectionUrl = "mongodb+srv://${Properties.get("mongo.user")}:${Properties.get("mongo.password")}@${Properties.get("mongo.host")}/${Properties.get("mongo.database")}?authSource=admin&tls=true"
    private val client = MongoClient(MongoClientURI(connectionUrl))

    private val codecs = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    private val database by lazy {
        client.getDatabase(Properties.get("mongo.database"))
            .withCodecRegistry(codecs)
    }

    fun <T> create(collection: String, entry: T, entryType: Class<T>) {
        database.getCollection(collection, entryType).insertOne(entry)
    }

    fun <T> readAllEntries(collection: String, entryType: Class<T>): List<T> {
        return database.getCollection(collection, entryType).find().toList()
    }

    fun <T> read(collection: String, entryType: Class<T>, condition: BasicDBObject): T {
        return database.getCollection(collection, entryType).find(condition).first()
    }

    fun <T> update(collection: String, entryType: Class<T>, condition: BasicDBObject, query: Bson) {
        database.getCollection(collection, entryType).updateOne(condition, query)
    }

    fun delete(collection: String, condition: BasicDBObject) {
        database.getCollection(collection).deleteOne(condition)
    }
}