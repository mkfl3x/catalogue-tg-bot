package database.mongo.models

import com.google.gson.JsonObject

interface MongoModel {

    fun asJson(): JsonObject
}