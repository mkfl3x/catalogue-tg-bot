package database.mongo.models.data

import com.google.gson.JsonObject
import database.mongo.models.MongoEntity
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

) : MongoEntity {

    override fun toJson() = JsonObject().apply {
        addProperty("id", id.toHexString())
        addProperty("name", name)
        addProperty("type", type)
        addProperty("data", data)
    }
}