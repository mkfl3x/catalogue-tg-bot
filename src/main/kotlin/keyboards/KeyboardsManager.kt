package keyboards

import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import database.Keyboard
import database.MongoClient
import utils.Properties

// TODO: add subscription for mongo keyboards collection update
object KeyboardsManager {

    // all available keyboards
    private val keyboards = mutableListOf<Keyboard>()

    init {
        keyboards.addAll(
            MongoClient.readAllEntries(
                Properties.get("mongo.collection.keyboards"),
                Keyboard::class.java
            )
        )
    }

    // returns list of all keyboard names
    fun getAllKeyboards(): List<String> = keyboards.map { it.name }.toList()

    // returns keyboard by name
    fun getKeyboard(name: String): Keyboard = keyboards.first { it.name == name }

    fun getKeyboardAsMarkup(name: String): ReplyKeyboardMarkup {
        val buttons = getKeyboard(name).buttons.map { KeyboardButton(it.text) }.toMutableList()
        if (name != "MainKeyboard")
            buttons.add(KeyboardButton("Back")) // each keyboard except MainKeyboard should have "Back" button
        return ReplyKeyboardMarkup(buttons.toTypedArray())
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
    }
}