package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SetWebhook
import utils.PropertiesManager

object Bot {

    private val bot = TelegramBot(PropertiesManager.get("bot.token"))

    fun init() {
        bot.execute(SetWebhook().url(PropertiesManager.get("bot.webhook")))
    }

    fun sendMessage(chatId: Long, messageText: String) {
        bot.execute(SendMessage(chatId, messageText))
    }
}