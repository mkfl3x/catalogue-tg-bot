package database.mongo.models.payload.inline

import com.google.gson.annotations.SerializedName
import com.pengrad.telegrambot.model.request.InlineKeyboardButton

data class InlineButton(
    @SerializedName("text") val text: String,
    @SerializedName("callback_data") val callbackData: String = ""
) {

    fun toMarkup(): InlineKeyboardButton = InlineKeyboardButton(text).callbackData(callbackData)
}