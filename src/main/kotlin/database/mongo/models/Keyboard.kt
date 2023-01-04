package database.mongo.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import common.ReservedNames
import database.mongo.DataManager.getButton
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Keyboard @BsonCreator constructor(

    @BsonId
    val id: ObjectId,

    @param:BsonProperty("name")
    @field:BsonProperty("name")
    val name: String,

    @param:BsonProperty("buttons")
    @field:BsonProperty("buttons")
    val buttons: List<ObjectId>,

    @param:BsonProperty("lead_buttons")
    @field:BsonProperty("lead_buttons")
    val leadButtons: List<ObjectId>

) : MongoEntity {

    override fun toJson() = JsonObject().apply {
        addProperty("id", id.toHexString())
        addProperty("name", name)
        if (name != ReservedNames.MAIN_KEYBOARD.text)
            add("lead_buttons", JsonArray().apply { leadButtons.forEach { add(it.toHexString()) } })
        add("buttons", JsonArray().apply { buttons.forEach { add(it.toHexString()) } })
    }

    fun toMarkup(): ReplyKeyboardMarkup {
        val buttons = buttonsToMarkup()
        if (name != ReservedNames.MAIN_KEYBOARD.text)
            buttons.add(KeyboardButton(ReservedNames.BACK.text))
        return ReplyKeyboardMarkup(buttons.toTypedArray())
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
    }

    fun fetchButtons() = buttons.map { getButton(it.toHexString()) }.toList()

    private fun buttonsToMarkup() = fetchButtons().map { KeyboardButton(it!!.text) }.toMutableSet()
}