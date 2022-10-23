package server.handlers

import bot.Bot
import bot.context.KeyboardStates
import com.pengrad.telegrambot.model.Update
import common.FeaturesList
import common.ReservedNames
import database.DataManager.getKeyboard
import database.DataManager.getKeyboards
import database.DataManager.getPayload
import database.models.Button
import integrations.Ml

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val chatId = update.message().chat().id()
        val message = update.message()
        if (message.voice() != null && FeaturesList.ML.enabled) {
            val link = bot.actions.getVoiceLink(message.voice().fileId())
            bot.actions.sendMessage(chatId, Ml.getAnswer(link, "voice", update.message().chat().id()))
            return
        } else {
            when (message.text()) {
                ReservedNames.START.text -> {
                    val startKeyboard = getKeyboards().find { it.name == ReservedNames.MAIN_KEYBOARD.text }
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
                        .firstOrNull { it!!.text == message.text() }?.let {
                            handleButtonClick(it, chatId)
                            return
                        }

                    if (FeaturesList.ML.enabled)
                        bot.actions.sendMessage(
                            chatId,
                            Ml.getAnswer(message.text(), "text", update.message().chat().id())
                        )
                    else
                        bot.actions.sendMessage(chatId, "Что то пошло не так \uD83E\uDD72")
                    return
                }
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