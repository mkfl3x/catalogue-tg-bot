package database.models

import com.google.gson.JsonObject
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Payload @BsonCreator constructor(
    @BsonId val id: ObjectId,
    @BsonProperty("name") val name: String,
    @BsonProperty("type") val type: String,
    @BsonProperty("data") val data: String
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