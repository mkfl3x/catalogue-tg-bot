import bot.Bot
import server.Server

fun main() {

    // TODO:
    //  - add logs
    //  - add mongo db
    //  - add dockerfile
    //  - add admin panel (react?)
    //  - add messages handling

    Bot.init()
    Server.start()
}