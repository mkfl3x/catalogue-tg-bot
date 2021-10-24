package database

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

enum class ButtonType(val type: String) {
    PAYLOAD("payload"),
    KEYBOARD("keyboard")
}

data class Keyboard @BsonCreator constructor(
    @BsonProperty(value = "name") val name: String,
    @BsonProperty(value = "buttons") val buttons: List<Button>
)

data class Button @BsonCreator constructor(
    @BsonProperty(value = "text") val text: String,
    @BsonProperty(value = "type") val type: ButtonType,
    @BsonProperty(value = "payload") val payload: String? = null,
    @BsonProperty(value = "keyboard") val keyboard: String? = null
)