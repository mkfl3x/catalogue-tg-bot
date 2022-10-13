package server.handlers

import bot.Bot
import com.pengrad.telegrambot.model.Update
import common.ReservedNames
import integrations.Ml
import keyboards.KeyboardsManager.getKeyboard
import keyboards.KeyboardsManager.keyboardStates
import keyboards.models.Button
import utils.FeaturesList

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        when (val message = update.message().text()) {
            ReservedNames.START.text -> {
                val startKeyboard = getKeyboard(ReservedNames.MAIN_KEYBOARD.text)
                keyboardStates.dropState(chatId)
                bot.actions.sendReplyKeyboard(chatId, startKeyboard!!)
                keyboardStates.addKeyboard(chatId, startKeyboard)
                return
            }
            ReservedNames.BACK.text -> {
                bot.actions.sendReplyKeyboard(chatId, keyboardStates.getPreviousKeyboard(chatId))
                return
            }
            else -> {
                keyboardStates.getCurrentKeyboard(chatId).buttons
                    .firstOrNull { it.text == message }?.let {
                        handleButtonClick(it, chatId)
                        return
                    }

                if (FeaturesList.ML.enabled)
                    bot.actions.sendMessage(chatId, Ml.getAnswer(message))
                else
                    bot.actions.sendMessage(chatId, "Что то пошло не так \uD83E\uDD72")
                return
            }
        }
    }

    private fun handleButtonClick(button: Button, chatId: Long) {
        when (button.type) {
            "payload" -> bot.actions.sendMessage(chatId, button.payload ?: "Empty button content")
            "keyboard" -> {
                val keyboard = getKeyboard(button.keyboard ?: throw Exception("Keyboard was not found"))
                bot.actions.sendReplyKeyboard(chatId, keyboard!!)
                keyboardStates.addKeyboard(chatId, keyboard)
            }
        }
    }
}