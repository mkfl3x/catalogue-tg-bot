package database.mongo.collections

import database.mongo.MongoClient
import database.mongo.MongoCollections
import database.mongo.models.MongoEntity

class MongoCollection<T : MongoEntity>(val name: MongoCollections, private val entityType: Class<T>) {

    lateinit var entities: HashSet<T>

    init {
        reload()
    }

    fun reload() {
        entities = MongoClient.readAllEntries(name.collectionName, entityType).toHashSet()
    }
}