package database.mongo.models.payload.messages

import com.google.gson.annotations.SerializedName

abstract class InlineDataMessage(

    @SerializedName("type")
    val type: String
)