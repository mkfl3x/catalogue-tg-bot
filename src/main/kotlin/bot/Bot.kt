package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SetWebhook

object Bot {

    // TODO: move bot token to config
    private val bot = TelegramBot("[bot_token]")

    fun init() {
        // TODO: move url to config
        bot.execute(SetWebhook().url("https://[your_server]/callback"))
    }

    fun sendMessage(chatId: Long, messageText: String) {
        bot.execute(SendMessage(chatId, messageText))
    }
}