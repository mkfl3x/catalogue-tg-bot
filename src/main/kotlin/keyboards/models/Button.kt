package keyboards.models

import com.google.gson.annotations.SerializedName
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

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