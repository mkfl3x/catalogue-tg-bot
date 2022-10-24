package server.handlers

import bot.Bot
import bot.context.KeyboardStates
import com.pengrad.telegrambot.model.Update
import common.FeaturesList
import common.ReservedNames
import database.mongo.DataManager.getKeyboard
import database.mongo.DataManager.getMainKeyboard
import database.mongo.DataManager.getPayload
import database.mongo.models.Button
import integrations.Ml
import org.bson.types.ObjectId

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val message = update.message()
        val chatId = message.chat().id()
        if (message.voice() != null && FeaturesList.ML.enabled) {
            val link = bot.actions.getVoiceLink(message.voice().fileId())
            bot.actions.sendMessage(chatId, Ml.getAnswer(link, "voice", update.message().chat().id()))
            return
        } else {
            when (message.text()) {
                ReservedNames.START.text -> {
                    KeyboardStates.dropState(chatId)
                    getMainKeyboard()?.let {
                        bot.actions.sendReplyKeyboard(chatId, it)
                        KeyboardStates.pushKeyboard(chatId, it.id)
                    }
                    return
                }
                ReservedNames.BACK.text -> {
                    KeyboardStates.getCurrentKeyboard(chatId)?.let {
                        if (getKeyboard(ObjectId(it))?.name == ReservedNames.MAIN_KEYBOARD.text)
                            return
                        else {
                            KeyboardStates.popKeyboard(chatId)
                            bot.actions.sendReplyKeyboard(
                                chatId,
                                getKeyboard(ObjectId(KeyboardStates.getCurrentKeyboard(chatId)))!!
                            )
                        }
                    }
                    return
                }
                else -> {
                    getKeyboard(ObjectId(KeyboardStates.getCurrentKeyboard(chatId)))!!.fetchButtons()
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
                KeyboardStates.pushKeyboard(chatId, keyboard.id)
            }
        }
    }
}