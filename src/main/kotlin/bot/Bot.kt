package bot

import com.pengrad.telegrambot.TelegramBot
import keyboards.KeyboardStates
import utils.Properties

class Bot {

    private val bot = TelegramBot(Properties.get("bot.token"))
    val actions = BotActions(this.bot)
    val keyboardStates = KeyboardStates()

    init {
        bot.execute(SetWebhook().url(Properties.get("bot.webhook.host") + Properties.get("bot.webhook.endpoint")))
    }
}