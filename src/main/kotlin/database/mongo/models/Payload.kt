package database.mongo.models

import com.google.gson.JsonObject
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Payload @BsonCreator constructor(

    @BsonId
    val id: ObjectId,

    @param:BsonProperty("name")
    @field:BsonProperty("name")
    val name: String,

    @param:BsonProperty("type")
    @field:BsonProperty("type")
    val type: String,

    @param:BsonProperty("data")
    @field:BsonProperty("data")
    val data: String

) : MongoModel {

    override fun asJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("id", id.toHexString())
        json.addProperty("name", name)
        json.addProperty("type", type)
        json.addProperty("data", data)
        return json
    }
}