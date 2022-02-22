## What is it?
It is a primitive application just for fun which provides containing keyboards and buttons on them.<br>
You can use it for organize some catalogue, as example.<br>
Feel free to contribute :)

## How does it work?
### Server
Provides API for managing keyboards and buttons. Based on *ktor*.

API methods description:
- [GET] */service/ping*<br>
Check is service available

- [GET] *keyboards/get* - Get all keyboards.<br>
Parameter ```filter``` has available values: *all*[by default](returns all keyboards0 and *detached*(returns only detached keyboards)
- [POST] */keyboards/add* - Add new keyboard.<br>
Keyboard might be added as detached(without *"host_keyboard"* field) and might be linked to already existing keyboard with button.
- [DELETE] */keyboards/delete* - Delete keyboard.<br>
If ```recursively=true``` then all nested keyboards and buttons will be deleted, if ```recursively=false``` all nested buttons will be deleted and all nested keyboards will be detached
- [PUT] */keyboards/detach* - Detach keyboard.<br>
Deleting *"host_keyboard"* field of keyboard and leding button to it from host keyboard
- [PUT] */keyboards/link* - Link keyboard to another one.<br>
Available only for detached keyboards.

- [POST] */buttons/add* - Add new button to keyboard.
- [DELETE] */buttons/delete* - Delete button from keyboard. If button linked to another keybord then keyboard became detached 

Examples of using API you can find in this [Postman collection](https://github.com/mkfl3x/tg-bot/blob/main/postman_collection.json)

### Telegram bot.
Used as UI for interactions with app.
Bot registering himself for Telegram server and handling incoming updated with webhook.<br>
For start using it type */start* in chat with bot. (Don't forget add some keyboards/buttons on 'MainKeyboard' before)

### MongoDB. 
Used for storage keyboards and buttons.

## How to build/run it?

### Local run/debug
  1. You need to have working MongoDB on your host machine with database **'data'** and collection **'keyboards'** filled with following object:<br>
  ```{"name": "MainKeyboard", "host_keyboard": "MainKeyboard", "buttons": []}```
  2. Fill/update **config.properties** file. You need to fill at least **bot.token**, **bot.webhook.host**, **mongo.host** and **mongo.port** properties<br>
  (Note that default values of **mongo.host** and **mongo.port** used for docker-compose)
  3. Run **main()** method from **Main.kt** file

### Containerizing
  1. build jar file with ```shadowJar``` Gradle task
  2. Build with docker-compose. Execute ```docker-compose build``` in your terminal
  3. Run with docker-compose. Execute ```BOT_TOKEN=%token% BOT_WEBHOOK_HOST=%webhook_host% docker-compose up``` in your terminal<br>
  4. You will get ready to use **mongo-db** (already initialized) and **tg-bot** containers on **localhost:8082**
  
## What then?
- Fix - Sending default text with keyboard messages.
- Fix - Deleting ALL buttons lead to deleted keyboard.
- Add Swagger for API docs
- Add full logs
- Write API tests
