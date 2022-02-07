package keyboards.models

import com.google.gson.annotations.SerializedName
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

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