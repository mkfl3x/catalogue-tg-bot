package database.mongo.models

import com.google.gson.JsonObject

interface MongoEntity {

    fun toJson(): JsonObject
}