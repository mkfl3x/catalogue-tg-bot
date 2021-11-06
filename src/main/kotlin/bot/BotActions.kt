package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.request.SendMessage

class BotActions(private val bot: TelegramBot) {

    fun sendMessage(chatId: Long, messageText: String) {
        bot.execute(SendMessage(chatId, messageText))
    }

    fun sendReplyKeyboard(chatId: Long, keyboard: Keyboard) {
        bot.execute(SendMessage(chatId, "Default text").replyMarkup(keyboard))
    }
}