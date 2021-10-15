package server

import bot.Bot
import com.pengrad.telegrambot.model.Update

class WebhookHandler {

    private val bot = Bot()

    fun handleUpdate(update: Update) {
        // TODO: debug mode - simple echo
        bot.actions.sendMessage(update.message().chat().id(), "echo: ${update.message().text()}")
    }
}