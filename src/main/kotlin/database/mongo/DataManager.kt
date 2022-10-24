package database.mongo

import common.ReservedNames
import database.mongo.models.Button
import database.mongo.models.Keyboard
import database.mongo.models.Payload
import org.bson.types.ObjectId

object DataManager {

    private lateinit var keyboards: HashSet<Keyboard>
    private lateinit var buttons: HashSet<Button>
    private lateinit var payloads: HashSet<Payload>

    init {
        reloadCollections()
    }

    fun reloadCollections() {
        MongoCollections.values().forEach { reloadCollection(it) }
    }

    private fun reloadCollection(collection: MongoCollections) {
        when (collection) {
            MongoCollections.KEYBOARDS -> keyboards =
                MongoClient.readAllEntries(collection.collectionName, Keyboard::class.java).toHashSet()
            MongoCollections.BUTTONS -> buttons =
                MongoClient.readAllEntries(collection.collectionName, Button::class.java).toHashSet()
            MongoCollections.PAYLOADS -> payloads =
                MongoClient.readAllEntries(collection.collectionName, Payload::class.java).toHashSet()
        }
    }

    fun getKeyboards() = keyboards

    fun getMainKeyboard() = keyboards.find { it.name == ReservedNames.MAIN_KEYBOARD.text }

    fun getKeyboard(id: ObjectId): Keyboard? = keyboards.firstOrNull { it.id == id }

    fun isKeyboardExist(id: ObjectId) = keyboards.any { it.id == id }

    fun keyboardHasButton(keyboard: ObjectId, buttonText: String) =
        getKeyboard(keyboard)!!.buttons
            .map { getButton(it) }
            .any { it!!.text == buttonText }

    fun getButtons() = buttons

    fun getButton(id: ObjectId): Button? = buttons.firstOrNull { it.id == id }

    fun getPayloads() = payloads

    fun getPayload(id: ObjectId): Payload? = payloads.firstOrNull { it.id == id }
}

