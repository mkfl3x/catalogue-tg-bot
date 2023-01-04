package server.handlers

import bot.Bot
import bot.context.KeyboardStates
import com.pengrad.telegrambot.model.Update
import common.ReservedNames
import database.mongo.DataManager.getKeyboard
import database.mongo.DataManager.getMainKeyboard
import database.mongo.DataManager.getPayload
import database.mongo.models.Button
import database.mongo.models.InlineKeyboard
import utils.GsonMapper

class WebhookHandler {

    companion object {
        private const val commonError = "Что то пошло не так \uD83E\uDD72"
        private val bot = Bot()
    }

    fun handleUpdate(update: Update) {
        update.callbackQuery()?.let {
            bot.actions.sendMessage(it.message().chat().id(), "echo callback_data: ${it.data()}")
            return
        }

        val message = update.message()
        val chatId = message.chat().id()
        message.text()?.let {
            when (it) {
                ReservedNames.START.text -> handleStart(chatId)
                ReservedNames.BACK.text -> handleBack(chatId)
                else -> handleMessage(chatId, message.text())
            }
            return
        }
    }

    private fun handleStart(chatId: Long) {
        KeyboardStates.dropState(chatId)
        getMainKeyboard()?.let {
            bot.actions.sendReplyKeyboard(chatId, it)
            KeyboardStates.pushKeyboard(chatId, it.id)
        }
    }

    private fun handleBack(chatId: Long) {
        KeyboardStates.getCurrentKeyboard(chatId)?.let {
            if (getKeyboard(it)?.name != ReservedNames.MAIN_KEYBOARD.text) {
                KeyboardStates.popKeyboard(chatId)
                bot.actions.sendReplyKeyboard(
                    chatId,
                    getKeyboard(KeyboardStates.getCurrentKeyboard(chatId))!!
                )
            }
        }
    }

    private fun handleMessage(chatId: Long, message: String) {
        getKeyboard(KeyboardStates.getCurrentKeyboard(chatId))?.let {
            it.fetchButtons().firstOrNull { button -> button!!.text == message }?.apply {
                handleButtonClick(chatId, this)
            }
        } ?: bot.actions.sendMessage(chatId, commonError)
    }

    private fun handleButtonClick(chatId: Long, button: Button) {
        when (button.type) {
            "payload" -> handlePayloadLink(chatId, button.linkTo.toHexString())
            "keyboard" -> handleKeyboardLink(chatId, button.linkTo.toHexString())
        }
    }

    private fun handlePayloadLink(chatId: Long, link: String) {
        getPayload(link)?.let {
            when (it.type) {
                "inline_keyboard" -> bot.actions.sendInlineKeyboard(
                    chatId,
                    GsonMapper.deserialize(it.data, InlineKeyboard::class.java)
                )

                "tutorial" -> bot.actions.sendMessage(chatId, it.data)
            }
        }
    }

    private fun handleKeyboardLink(chatId: Long, link: String) {
        getKeyboard(link)?.let {
            bot.actions.sendReplyKeyboard(chatId, it)
            KeyboardStates.pushKeyboard(chatId, it.id)
        }
    }
}