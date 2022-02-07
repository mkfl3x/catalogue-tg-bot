package keyboards

import com.google.gson.annotations.SerializedName
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

data class Keyboard @BsonCreator constructor(

    @SerializedName("name")
    @param:BsonProperty("name")
    @field:BsonProperty("name")
    val name: String,

    // keyboardLocation == null means keyboard is detached
    @SerializedName("keyboard_location")
    @param:BsonProperty("keyboard_location")
    @field:BsonProperty("keyboard_location")
    val keyboardLocation: KeyboardLocation?,

    @SerializedName("buttons")
    @param:BsonProperty("buttons")
    @field:BsonProperty("buttons")
    val buttons: List<Button>
)

data class KeyboardLocation @BsonCreator constructor(

    @SerializedName("host_keyboard")
    @param:BsonProperty("host_keyboard")
    @field:BsonProperty("host_keyboard")
    val hostKeyboard: String,

    @SerializedName("link_button")
    @param:BsonProperty("link_button")
    @field:BsonProperty("link_button")
    val linkButton: String,
)

data class Button @BsonCreator constructor(
    @SerializedName("text")
    @param:BsonProperty("text")
    @field:BsonProperty("text")
    val text: String,

    @SerializedName("type")
    @param:BsonProperty("type")
    @field:BsonProperty("type")
    val type: String,

    @SerializedName("payload")
    @param:BsonProperty("payload")
    @field:BsonProperty("payload")
    val payload: String? = null,

    @SerializedName("keyboard")
    @param:BsonProperty("keyboard")
    @field:BsonProperty("keyboard")
    val keyboard: String? = null
)