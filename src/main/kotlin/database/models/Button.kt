package database.models

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Button @BsonCreator constructor(

    @param:BsonProperty("_id")
    @field:BsonProperty("_id")
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
)