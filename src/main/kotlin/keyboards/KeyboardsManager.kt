package keyboards

import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import database.MongoClient
import utils.Properties

object KeyboardsManager {

    private val keyboards: MutableList<Keyboard> = mutableListOf()

    init {
        uploadKeyboards()
    }

    fun getKeyboards(): List<Keyboard> = keyboards

    fun getKeyboard(name: String): Keyboard? = keyboards.firstOrNull { it.name == name }

    fun getKeyboardAsMarkup(name: String): ReplyKeyboardMarkup {
        val buttons = getKeyboard(name)?.buttons?.map { KeyboardButton(it.text) }?.toMutableList()
        if (name != "MainKeyboard")
            buttons?.add(KeyboardButton("Back")) // each keyboard except MainKeyboard should have "Back" button
        return ReplyKeyboardMarkup(buttons?.toTypedArray())
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
    }

    private fun uploadKeyboards() {
        keyboards.addAll(
            MongoClient.readAllEntries(
                Properties.get("mongo.collection.keyboards"),
                Keyboard::class.java
            )
        )
    }
}