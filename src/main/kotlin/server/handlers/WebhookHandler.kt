package server.handlers

import bot.Bot
import com.pengrad.telegrambot.model.Update
import keyboards.KeyboardsManager

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        val message = update.message().text()

        when (message) {
            "/start" -> {
                bot.keyboardStates.dropState(chatId)
                bot.actions.sendReplyKeyboard(chatId, KeyboardsManager.getKeyboardAsMarkup("MainKeyboard"))
                bot.keyboardStates.addKeyboard(chatId, "MainKeyboard")
                return
            }
            "Back" -> {
                bot.actions.sendReplyKeyboard(
                    chatId,
                    KeyboardsManager.getKeyboardAsMarkup(bot.keyboardStates.getPreviousKeyboard(chatId))
                )
                return
            }
            else -> {
                val keyboard = KeyboardsManager.getKeyboard(bot.keyboardStates.getCurrentKeyboard(chatId))
                if (keyboard!!.buttons.firstOrNull { it.text == message } == null)
                    bot.actions.sendMessage(chatId, "Not recognized command")
            }
        }

        val currentKeyboard = KeyboardsManager.getKeyboard(bot.keyboardStates.getCurrentKeyboard(chatId))
        val currentKeyboardButton = currentKeyboard!!.buttons.firstOrNull { it.text == message }
        if (currentKeyboardButton != null) {
            when (currentKeyboardButton.type) {
                "payload" -> {
                    bot.actions.sendMessage(chatId, currentKeyboardButton.payload!!)
                }
                "keyboard" -> {
                    val keyboard = KeyboardsManager.getKeyboardAsMarkup(currentKeyboardButton.keyboard!!)
                    bot.actions.sendReplyKeyboard(chatId, keyboard)
                    bot.keyboardStates.addKeyboard(chatId, currentKeyboardButton.keyboard)
                }
            }
        } else {
            throw Exception("Button '$message' was not found on current keyboard")
        }
    }
}