package keyboards.models

import com.google.gson.annotations.SerializedName
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
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
) {

    fun toMarkup(): ReplyKeyboardMarkup {
        val buttons = buttons.map { KeyboardButton(it.text) }.toMutableSet()
        if (name != "MainKeyboard")
            buttons.add(KeyboardButton("Back"))
        return ReplyKeyboardMarkup(buttons.toTypedArray())
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
    }
}