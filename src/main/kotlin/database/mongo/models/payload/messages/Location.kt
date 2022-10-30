package database.mongo.models.payload.messages

import com.google.gson.annotations.SerializedName
import database.mongo.models.payload.inline.InlineKeyboard

class Location(

    @SerializedName("latitude")
    val latitude: Float,

    @SerializedName("longitude")
    val longitude: Float,

    @SerializedName("inline_keyboard")
    val inlineKeyboard: InlineKeyboard?

) : InlineDataMessage("location")