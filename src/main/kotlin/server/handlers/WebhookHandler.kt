package server.handlers

import bot.Bot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import database.ButtonType
import database.Keyboard

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        val message = update.message().text()

        when (message) {
            "/start" -> { // TODO: this message should be placed as button
                bot.actions.sendReplyKeyboard(chatId, getKeyboardAsMarkup("MainKeyboard"))
                bot.states.addKeyboard(chatId, "MainKeyboard")
                return
            }
            "Back" -> {
                bot.states.removeKeyboard(chatId)
                bot.actions.sendReplyKeyboard(chatId, getKeyboardAsMarkup(bot.states.getLastKeyboard(chatId)))
                return
            }
        }

        // check is current keyboard contains button with message text and interact with it
        val currentKeyboard = getKeyboard(bot.states.getLastKeyboard(chatId))
        val currentKeyboardButton = currentKeyboard.buttons.firstOrNull { it.text == message }
        if (currentKeyboardButton != null) {
            when (currentKeyboardButton.type) {
                ButtonType.PAYLOAD -> {
                    bot.actions.sendMessage(chatId, currentKeyboardButton.payload!!)
                }
                ButtonType.KEYBOARD -> {
                    val keyboard = getKeyboardAsMarkup(currentKeyboardButton.keyboard!!)
                    bot.actions.sendReplyKeyboard(chatId, keyboard)
                    bot.states.addKeyboard(chatId, currentKeyboardButton.keyboard)
                }
            }
        } else {
            throw Exception("Button '$message' was not found on current keyboard")
        }
    }

    private fun getKeyboardAsMarkup(name: String): ReplyKeyboardMarkup {
        val buttons = getKeyboard(name).buttons.map { KeyboardButton(it.text) }.toMutableList()
        if (name != "MainKeyboard")
            buttons.add(KeyboardButton("Back"))
        return ReplyKeyboardMarkup(buttons.toTypedArray())
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
    }

    // TODO: handle case when keyboard not found
    private fun getKeyboard(name: String): Keyboard = bot.keyboards.first { it.name == name }
}