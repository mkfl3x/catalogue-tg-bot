package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SetWebhook
import keyboards.KeyboardStates
import utils.Properties

class Bot {

    private val bot = TelegramBot(Properties.get("bot.token"))

    val actions = BotActions(this.bot)

    val keyboardStates = KeyboardStates()  // TODO: it should be moved to Redis

    init {
        // registering bot webhook endpoint at telegram
        bot.execute(SetWebhook().url(Properties.get("bot.webhook.host") + Properties.get("bot.webhook.endpoint")))
    }
}