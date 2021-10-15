package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.request.SendMessage

class BotActions(private val bot: TelegramBot) {

    fun sendTextMessage(chatId: Long, messageText: String) {
        bot.execute(SendMessage(chatId, messageText))
    }

    fun sendKeyboard(chatId: Long, title:String, keyboard: Keyboard) {
        bot.execute(SendMessage(chatId, title).replyMarkup(keyboard))
    }
}