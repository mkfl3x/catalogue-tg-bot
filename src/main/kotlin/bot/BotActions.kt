package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import keyboards.models.Keyboard

class BotActions(private val bot: TelegramBot) {

    fun sendMessage(chatId: Long, messageText: String) {
        bot.execute(SendMessage(chatId, messageText))
    }

    fun sendReplyKeyboard(chatId: Long, keyboard: Keyboard) {
        bot.execute(SendMessage(chatId, keyboard.name).replyMarkup(keyboard.toMarkup()))
    }
}