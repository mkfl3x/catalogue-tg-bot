package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SetWebhook
import utils.Properties

class Bot {

    private val bot = TelegramBot(Properties.get("bot.token"))
    val actions = BotActions(this.bot)

    init {
        bot.execute(
            SetWebhook()
                .url(Properties.get("bot.webhook.host") + Properties.get("bot.webhook.endpoint"))
                .secretToken(Properties.get("bot.webhook.secret"))
        )
    }
}