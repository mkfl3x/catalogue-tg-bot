package database.mongo.models

import com.google.gson.annotations.SerializedName
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

data class InlineKeyboard(
    @SerializedName("message") val message: String,
    @SerializedName("rows") val rows: List<List<InlineButton>>
) {

    fun toMarkup() = InlineKeyboardMarkup().apply {
        rows.forEach { row -> this.addRow(*row.map { it.toMarkup() }.toTypedArray()) }
    }
}