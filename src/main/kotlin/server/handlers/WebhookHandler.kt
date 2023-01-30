package server.handlers

import bot.Bot
import bot.context.KeyboardStates
import com.pengrad.telegrambot.model.Update
import common.ReservedNames
import database.mongo.managers.DataManager.getKeyboard
import database.mongo.managers.DataManager.getMainKeyboard
import database.mongo.managers.DataManager.getPayload
import database.mongo.models.data.Button

class WebhookHandler : RequestHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        val message = update.message()
        val chatId = message.chat().id()
        try {
            message.text()?.let {
                when (it) {
                    ReservedNames.START.text -> handleStart(chatId)
                    ReservedNames.BACK.text -> handleBack(chatId)
                    else -> handleMessage(chatId, message.text())
                }
            }
        } catch (e: Exception) {
            // TODO: add exception log
            bot.actions.sendMessage(chatId, commonError)
        }
    }

    private fun handleStart(chatId: Long) {
        KeyboardStates.dropState(chatId)
        getMainKeyboard().apply {
            bot.actions.sendReplyKeyboard(chatId, this)
            KeyboardStates.pushKeyboard(chatId, this.id)
        }
    }

    private fun handleBack(chatId: Long) {
        KeyboardStates.getCurrentKeyboard(chatId)?.apply {
            if (getKeyboard(this).name != ReservedNames.MAIN_KEYBOARD.text) {
                KeyboardStates.popKeyboard(chatId)
                bot.actions.sendReplyKeyboard(
                    chatId,
                    getKeyboard(KeyboardStates.getCurrentKeyboard(chatId)!!)
                )
            }
        }
    }

    private fun handleMessage(chatId: Long, message: String) {
        getKeyboard(KeyboardStates.getCurrentKeyboard(chatId)!!).apply {
            fetchButtons().firstOrNull { button -> button.text == message }?.apply {
                handleButtonClick(chatId, this)
            }
        }
    }

    private fun handleButtonClick(chatId: Long, button: Button) {
        when (button.type) {
            "payload" -> handlePayloadLink(chatId, button.linkTo.toHexString())
            "keyboard" -> handleKeyboardLink(chatId, button.linkTo.toHexString())
        }
    }

    private fun handlePayloadLink(chatId: Long, link: String) {
        getPayload(link).apply {
            when (type) {
                "tutorial" -> bot.actions.sendMessage(chatId, data)
            }
        }
    }

    private fun handleKeyboardLink(chatId: Long, link: String) {
        getKeyboard(link).apply {
            bot.actions.sendReplyKeyboard(chatId, this)
            KeyboardStates.pushKeyboard(chatId, this.id)
        }
    }
}