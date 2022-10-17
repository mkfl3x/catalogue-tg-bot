package server.handlers

import bot.Bot
import com.pengrad.telegrambot.model.Update
import common.FeaturesList
import common.ReservedNames
import database.DataManager.getKeyboard
import database.DataManager.getPayload
import database.models.Button
import integrations.Ml
import bot.context.KeyboardStates

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        when (val message = update.message().text()) {
            ReservedNames.START.text -> {
                val startKeyboard = getKeyboard(ReservedNames.MAIN_KEYBOARD.text)
                KeyboardStates.dropState(chatId)
                bot.actions.sendReplyKeyboard(chatId, startKeyboard!!)
                KeyboardStates.addKeyboard(chatId, startKeyboard)
                return
            }
            ReservedNames.BACK.text -> {
                bot.actions.sendReplyKeyboard(chatId, KeyboardStates.getPreviousKeyboard(chatId))
                return
            }
            else -> {
                KeyboardStates.getCurrentKeyboard(chatId).fetchButtons()
                    .firstOrNull { it!!.text == message }?.let {
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
            "payload" -> bot.actions.sendMessage(chatId, getPayload(button.linkTo)!!.data)
            "keyboard" -> {
                val keyboard = getKeyboard(button.linkTo) ?: throw Exception("Keyboard was not found")
                bot.actions.sendReplyKeyboard(chatId, keyboard)
                KeyboardStates.addKeyboard(chatId, keyboard)
            }
        }
    }
}