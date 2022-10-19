package database.models

import com.google.gson.JsonObject
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Button @BsonCreator constructor(
    @BsonId val id: ObjectId,
    @BsonProperty("text") val text: String,
    @BsonProperty("type") val type: String,
    @BsonProperty("link_to") val linkTo: ObjectId
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