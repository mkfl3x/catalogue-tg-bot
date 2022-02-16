package server.handlers

import bot.Bot
import com.pengrad.telegrambot.model.Update
import common.ReservedNames
import keyboards.KeyboardsManager.getKeyboard
import keyboards.KeyboardsManager.keyboardStates
import keyboards.models.Button

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        when (val message = update.message().text()) {
            ReservedNames.START.text -> {
                val startKeyboard = getKeyboard(ReservedNames.MAIN_KEYBOARD.text)
                keyboardStates.dropState(chatId)
                bot.actions.sendReplyKeyboard(chatId, startKeyboard!!.toMarkup())
                keyboardStates.addKeyboard(chatId, startKeyboard)
                return
            }
            ReservedNames.BACK.text -> {
                bot.actions.sendReplyKeyboard(chatId, keyboardStates.getPreviousKeyboard(chatId).toMarkup())
                return
            }
            else -> {
                keyboardStates.getCurrentKeyboard(chatId).buttons
                    .firstOrNull { it.text == message }?.let {
                        handleButtonClick(it, chatId)
                        return
                    }
                bot.actions.sendMessage(chatId, "Unknown command")
                return
            }
        }
    }

    private fun handleButtonClick(button: Button, chatId: Long) {
        when (button.type) {
            "payload" -> bot.actions.sendMessage(chatId, button.payload ?: "Empty button content")
            "keyboard" -> {
                val keyboard = getKeyboard(button.keyboard ?: throw Exception("Keyboard was not found"))
                bot.actions.sendReplyKeyboard(chatId, keyboard!!.toMarkup())
                keyboardStates.addKeyboard(chatId, keyboard)
            }
        }
    }
}