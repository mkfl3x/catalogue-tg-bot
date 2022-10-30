package database.mongo.models.payload.messages

import com.google.gson.annotations.SerializedName
import database.mongo.models.payload.inline.InlineKeyboard

class Text(

    @SerializedName("text")
    val text: String,

    @SerializedName("inline_keyboard")
    val inlineKeyboard: InlineKeyboard?

) : InlineDataMessage("text")