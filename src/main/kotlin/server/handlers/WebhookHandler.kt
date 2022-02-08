package server.handlers

import bot.Bot
import com.pengrad.telegrambot.model.Update
import common.ReservedNames
import keyboards.KeyboardsManager

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        val message = update.message().text()

        when (message) {
            ReservedNames.START.text -> {
                val startKeyboard = KeyboardsManager.getKeyboard(ReservedNames.MAIN_KEYBOARD.text)
                KeyboardsManager.keyboardStates.dropState(chatId)
                bot.actions.sendReplyKeyboard(chatId, startKeyboard!!.toMarkup())
                KeyboardsManager.keyboardStates.addKeyboard(chatId, startKeyboard)
                return
            }
            ReservedNames.BACK.text -> {
                bot.actions.sendReplyKeyboard(
                    chatId,
                    KeyboardsManager.keyboardStates.getPreviousKeyboard(chatId).toMarkup()
                )
                return
            }
            else -> {
                val keyboard = KeyboardsManager.keyboardStates.getCurrentKeyboard(chatId)
                if (keyboard.buttons.firstOrNull { it.text == message } == null)
                    bot.actions.sendMessage(chatId, "Unknown command")
            }
        }

        val currentKeyboard = KeyboardsManager.keyboardStates.getCurrentKeyboard(chatId)
        val currentKeyboardButton = currentKeyboard.buttons.firstOrNull { it.text == message }
        if (currentKeyboardButton != null) {
            when (currentKeyboardButton.type) {
                "payload" -> {
                    bot.actions.sendMessage(chatId, currentKeyboardButton.payload!!)
                }
                "keyboard" -> {
                    val keyboard = KeyboardsManager.getKeyboard(currentKeyboardButton.keyboard!!)
                    bot.actions.sendReplyKeyboard(chatId, keyboard!!.toMarkup())
                    KeyboardsManager.keyboardStates.addKeyboard(chatId, keyboard)
                }
            }
        } else {
            throw Exception("Button '$message' was not found on current keyboard")
        }
    }
}