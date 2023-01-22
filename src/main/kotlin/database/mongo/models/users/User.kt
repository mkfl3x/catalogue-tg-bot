package database.mongo.models.users

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class User @BsonCreator constructor(

    @BsonId
    val id: ObjectId,

    @param:BsonProperty("username")
    @field:BsonProperty("username")
    val username: String,

    @param:BsonProperty("password")
    @field:BsonProperty("password")
    val password: String,

    @param:BsonProperty("role")
    @field:BsonProperty("role")
    val role: String
)