package keyboards

import com.google.gson.annotations.SerializedName
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

data class Keyboard @BsonCreator constructor(
    @BsonProperty(value = "name")
    @SerializedName("name")
    val name: String,

    @SerializedName("host_keyboard")
    @BsonProperty(value = "host_keyboard")
    val hostKeyboard: String,

    @SerializedName("buttons")
    @BsonProperty(value = "buttons")
    val buttons: List<Button>
)

data class Button @BsonCreator constructor(
    @SerializedName("text")
    @BsonProperty(value = "text")
    val text: String,

    @SerializedName("type")
    @BsonProperty(value = "type")
    val type: String,

    @SerializedName("payload")
    @BsonProperty(value = "payload")
    val payload: String? = null,

    @SerializedName("keyboard")
    @BsonProperty(value = "keyboard")
    val keyboard: String? = null
)