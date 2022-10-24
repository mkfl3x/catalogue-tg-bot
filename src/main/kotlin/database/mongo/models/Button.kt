package database.mongo.models

import com.google.gson.JsonObject
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Button @BsonCreator constructor(

    @BsonId
    val id: ObjectId,

    @param:BsonProperty("text")
    @field:BsonProperty("text")
    val text: String,

    @param:BsonProperty("type")
    @field:BsonProperty("type")
    val type: String,

    @param:BsonProperty("link_to")
    @field:BsonProperty("link_to")
    val linkTo: ObjectId

) : MongoModel {

    override fun asJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("id", id.toHexString())
        json.addProperty("text", text)
        json.addProperty("type", type)
        json.addProperty("link_to", linkTo.toHexString())
        return json
    }
}