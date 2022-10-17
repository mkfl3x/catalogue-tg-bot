package database.models

import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import common.ReservedNames
import database.DataManager.getButton
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Keyboard @BsonCreator constructor(

    @param:BsonProperty("_id")
    @field:BsonProperty("_id")
    val id: ObjectId,

    @param:BsonProperty("name")
    @field:BsonProperty("name")
    val name: String,

    @param:BsonProperty("buttons")
    @field:BsonProperty("buttons")
    val buttons: List<ObjectId>,

    @param:BsonProperty("lead_buttons")
    @field:BsonProperty("lead_buttons")
    val leadButton: ObjectId?
) {

    fun toMarkup(): ReplyKeyboardMarkup {
        val buttons = buttonsToMarkup()
        if (name != ReservedNames.MAIN_KEYBOARD.text)
            buttons.add(KeyboardButton(ReservedNames.BACK.text))
        return ReplyKeyboardMarkup(buttons.toTypedArray())
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
    }

    fun fetchButtons() = buttons.map { getButton(it) }.toList()

    private fun buttonsToMarkup() = fetchButtons().map { KeyboardButton(it!!.text) }.toMutableSet()
}