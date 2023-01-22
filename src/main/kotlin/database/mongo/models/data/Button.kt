package database.mongo.models.data

import com.google.gson.JsonObject
import database.mongo.models.MongoEntity
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
    val type: String, // TODO: use enum

    @param:BsonProperty("link_to")
    @field:BsonProperty("link_to")
    val linkTo: ObjectId

) : MongoEntity {

    override fun toJson() = JsonObject().apply {
        addProperty("id", id.toHexString())
        addProperty("text", text)
        addProperty("type", type)
        addProperty("link_to", linkTo.toHexString())
    }
}