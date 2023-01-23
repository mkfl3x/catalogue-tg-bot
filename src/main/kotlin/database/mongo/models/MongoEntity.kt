package database.mongo.models

import com.google.gson.JsonObject

interface MongoEntity {

    // TODO: try not to use explicitly
    fun toJson(): JsonObject
}