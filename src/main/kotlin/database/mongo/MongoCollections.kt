package database.mongo

import database.mongo.models.Button
import database.mongo.models.Keyboard
import database.mongo.models.Payload

enum class MongoCollections(val collectionName: String, val type: Class<*>) {
    KEYBOARDS("keyboards", Keyboard::class.java),
    BUTTONS("buttons", Button::class.java),
    PAYLOADS("payloads", Payload::class.java)
}