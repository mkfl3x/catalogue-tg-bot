package database.models

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Payload @BsonCreator constructor(

    @param:BsonProperty("_id")
    @field:BsonProperty("_id")
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
)