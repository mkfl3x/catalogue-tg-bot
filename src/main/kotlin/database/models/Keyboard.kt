package database.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import common.ReservedNames
import database.DataManager.getButton
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Keyboard @BsonCreator constructor(
    @BsonId val id: ObjectId,
    @BsonProperty("name") val name: String,
    @BsonProperty("buttons") val buttons: List<ObjectId>,
    @BsonProperty("lead_button") val leadButton: ObjectId?
) : MongoModel {

    override fun asJson(): JsonObject {
        val buttons = JsonArray()
        this.buttons.forEach { buttons.add(it.toHexString()) }
        val json = JsonObject()
        json.addProperty("id", id.toHexString())
        json.addProperty("name", name)
        json.addProperty("lead_button", leadButton?.toHexString())
        json.add("buttons", buttons)
        return json
    }

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