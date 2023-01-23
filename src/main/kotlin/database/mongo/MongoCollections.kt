package database.mongo

import database.mongo.models.MongoEntity
import database.mongo.models.data.Button
import database.mongo.models.data.Keyboard
import database.mongo.models.data.Payload
import database.mongo.models.users.User

enum class MongoCollections(val collectionName: String, val entityType: Class<out MongoEntity>) {
    KEYBOARDS("keyboards", Keyboard::class.java),
    BUTTONS("buttons", Button::class.java),
    PAYLOADS("payloads", Payload::class.java),
    USERS("users", User::class.java)
}