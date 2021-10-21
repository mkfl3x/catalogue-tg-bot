package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SetWebhook
import database.Keyboard
import database.MongoClient
import utils.Properties

class Bot {

    private val bot = TelegramBot(Properties.get("bot.token"))

    val actions = BotActions(this.bot)

    val keyboards = MongoClient.read(Properties.get("mongo.collection.keyboards"), Keyboard::class.java)

    // TODO: it should be moved to Redis
    val states = KeyboardStates()

    init {
        bot.execute(SetWebhook().url(Properties.get("bot.webhook.host") + Properties.get("bot.webhook.endpoint")))
    }
}