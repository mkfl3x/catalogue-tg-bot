package keyboards.models

import com.google.gson.annotations.SerializedName
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

data class Keyboard @BsonCreator constructor(

    @SerializedName("name")
    @param:BsonProperty("name")
    @field:BsonProperty("name")
    val name: String,

    @SerializedName("keyboard_location")
    @param:BsonProperty("keyboard_location")
    @field:BsonProperty("keyboard_location")
    val keyboardLocation: KeyboardLocation?,

    @SerializedName("buttons")
    @param:BsonProperty("buttons")
    @field:BsonProperty("buttons")
    val buttons: List<Button>
)