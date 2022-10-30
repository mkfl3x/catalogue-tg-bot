package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.GetFile
import com.pengrad.telegrambot.request.SendMessage
import database.mongo.models.Keyboard

class BotActions(private val bot: TelegramBot) {

    fun sendMessage(chatId: Long, messageText: String) {
        bot.execute(SendMessage(chatId, messageText))
    }

    fun sendReplyKeyboard(chatId: Long, keyboard: Keyboard) {
        bot.execute(SendMessage(chatId, keyboard.name).replyMarkup(keyboard.toMarkup()))
    }

    fun sendPayload(message: AbstractSendRequest<*>) {
        bot.execute(message)
    }

    fun getVoiceLink(fileId: String): String = bot.getFullFilePath(bot.execute(GetFile(fileId)).file())
}