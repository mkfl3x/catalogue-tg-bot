package database.mongo.models.payload.messages

import com.google.gson.annotations.SerializedName
import database.mongo.models.payload.inline.InlineKeyboard

class Image(

    @SerializedName("caption")
    val caption: String?,

    @SerializedName("link")
    val link: String,

    @SerializedName("inline_keyboard")
    val inlineKeyboard: InlineKeyboard?

) : InlineDataMessage("images")