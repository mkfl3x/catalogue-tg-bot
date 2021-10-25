package database

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

data class Keyboard @BsonCreator constructor(
    @BsonProperty(value = "name") val name: String,
    @BsonProperty(value = "buttons") val buttons: List<Button>
)

data class Button @BsonCreator constructor(
    @BsonProperty(value = "text") val text: String,
    @BsonProperty(value = "type") val type: String,
    @BsonProperty(value = "payload") val payload: String? = null,
    @BsonProperty(value = "keyboard") val keyboard: String? = null
)