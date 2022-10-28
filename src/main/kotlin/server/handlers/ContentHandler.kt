package server.handlers

import bot.context.KeyboardStates
import com.mongodb.BasicDBObject
import database.mongo.DataManager
import database.mongo.MongoClient
import database.mongo.MongoCollections
import database.mongo.models.Button
import database.mongo.models.Keyboard
import database.mongo.models.Payload
import org.bson.BsonNull
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.models.Error
import server.models.Result
import server.models.requests.*
import utils.GsonMapper

class ContentHandler {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    fun handleRequest(data: String, request: Requests): Result {
        RequestValidator.validateSchema(data, request.schemaPath)?.let { return it }
        (GsonMapper.deserialize(data, request.type) as Request).apply {
            validateData()?.let { return it }
            when (this) {
                is AddKeyboardRequest -> addKeyboard(this)
                is AddButtonRequest -> addButton(this)
                is AddPayloadRequest -> addPayload(this)
                is DeleteKeyboardRequest -> deleteKeyboard(this)
                is DeleteButtonRequest -> deleteButton(this)
                is DeletePayloadRequest -> deletePayload(this)
                is LinkButtonRequest -> linkButton(this)
                is DetachKeyboardRequest -> detachKeyboard(this)
                else -> throw Exception("Unknown request type")
            }
            DataManager.reloadCollections()
            return Result.success(this.successMessage)
        }
    }

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> DataManager.getKeyboards()
            "detached" -> DataManager.getKeyboards()
                .filter { it.leadButton == null }
                .toList()
            else -> return Result.error(Error.UNKNOWN_PARAMETER_VALUE, filter, "filter")
        }
        return Result.success(keyboards.map { it.toJson() }.toList())
    }

    fun getButtons(filter: String): Result {
        val buttons = when (filter) {
            "all" -> DataManager.getButtons()
            else -> return Result.error(Error.UNKNOWN_PARAMETER_VALUE, filter, "filter")
        }
        return Result.success(buttons.map { it.toJson() }.toList())
    }

    fun getPayloads(): Result {
        return Result.success(DataManager.getPayloads().map { it.toJson() }.toList())
    }

    private fun addKeyboard(keyboard: AddKeyboardRequest) {
        // 1. Create keyboard
        val newKeyboardId = createKeyboard(Keyboard(ObjectId(), keyboard.name, emptyList(), null))
        // 3. Add leading keyboard button (if it's exist)
        keyboard.location?.let {
            val buttonId = createButton(Button(ObjectId(), it.leadButtonText, "keyboard", newKeyboardId))
            // 3.1 Add button on host keyboard
            addButtonToKeyboard(buttonId, ObjectId(it.hostKeyboard))
            // 3.2 Add leading button to new keyboard
            addButtonToKeyboard(buttonId, newKeyboardId, leadButton = true)
        }
        // 4. Update keyboard's  content buttons
        keyboard.buttons.forEach { addButtonToKeyboard(ObjectId(it), newKeyboardId) }
    }

    private fun deleteKeyboard(request: DeleteKeyboardRequest) {
        DataManager.getKeyboard(request.keyboardId)?.let {
            // 1. Drop keyboard from states
            KeyboardStates.deleteKeyboard(request.keyboardId)
            // 2. Delete keyboard from collection
            deleteKeyboard(it.id)
            // 3. Delete lead button (if it's exist)
            it.leadButton?.let { leadButton ->
                // 3.1 Delete keyboard's lead button
                deleteButton(leadButton)
                // 3.2 Delete lead button from all keyboards which contains it
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.buttons.contains(leadButton) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(leadButton, keyboard.id) }
            }
        }
    }

    private fun detachKeyboard(request: DetachKeyboardRequest) {
        DataManager.getKeyboard(request.keyboardId)?.let {
            it.leadButton?.let { button ->
                // 1. Delete lead button from buttons collection
                deleteButton(button)
                // 2. Update keyboard lead button field
                deleteButtonFromKeyboard(button, it.id, leadButton = true)
                // 3. Delete lead button from all keyboards which contains it
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.buttons.contains(button) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(button, keyboard.id) }
            }
        }
    }

    private fun addButton(button: AddButtonRequest) {
        // 1. Create new detached button
        val newButton = createButton(Button(ObjectId(), button.text, button.type, ObjectId(button.link)))
        // 2. Put button to some keyboard is needed
        button.hostKeyboard?.let { addButtonToKeyboard(newButton, ObjectId(it)) }
    }

    private fun deleteButton(request: DeleteButtonRequest) {
        val button = DataManager.getButton(request.buttonId)!!
        // 1. Detach keyboard, if it's lead button
        DataManager.getKeyboards()
            .find { keyboard -> keyboard.leadButton == button.id }
            ?.let { deleteButtonFromKeyboard(button.id, it.id, leadButton = true) }
        // 2. Delete button from collection
        deleteButton(button.id)
        // 3. Delete button from all keyboards
        DataManager.getKeyboards()
            .filter { keyboard -> keyboard.buttons.contains(button.id) }
            .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
    }

    private fun linkButton(request: LinkButtonRequest) {
        MongoClient.update(
            MongoCollections.BUTTONS.collectionName,
            Button::class.java,
            BasicDBObject("_id", ObjectId(request.buttonId)),
            BasicDBObject("\$set", BasicDBObject("link_to", ObjectId(request.link)))
        )
        MongoClient.update(
            MongoCollections.BUTTONS.collectionName,
            Button::class.java,
            BasicDBObject("_id", ObjectId(request.buttonId)),
            BasicDBObject("\$set", BasicDBObject("type", request.type))
        )
    }

    fun addPayload(payload: AddPayloadRequest) {
        // 1. Create payload
        val payloadId = addPayload(Payload(ObjectId(), payload.name, payload.type, payload.data))
        // 2. Add leading keyboard button (if it's exist)
        payload.location?.let {
            val buttonId = createButton(Button(ObjectId(), it.leadButtonText, "payload", payloadId))
            // 2.1 Add button to host keyboard
            addButtonToKeyboard(buttonId, ObjectId(it.hostKeyboard))
        }
    }

    private fun deletePayload(request: DeletePayloadRequest) {
        DataManager.getPayload(request.payloadId)?.let {
            // 1. Delete payload from collection
            deletePayload(it.id)

            // 2. Delete all button usages from keyboards
            DataManager.getButtons()
                .filter { button -> button.linkTo == it.id }
                .forEach { button ->
                    DataManager.getKeyboards()
                        .filter { keyboard -> keyboard.buttons.contains(button.id) }
                        .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
                }

            // 3. Delete buttons, leads to payload
            DataManager.getButtons()
                .filter { button -> button.linkTo == it.id }
                .forEach { button -> deleteButton(button.id) }
        }
    }

    private fun createKeyboard(keyboard: Keyboard): ObjectId {
        MongoClient.create(MongoCollections.KEYBOARDS.collectionName, keyboard, Keyboard::class.java)
        return keyboard.id
    }

    private fun deleteKeyboard(keyboardId: ObjectId) {
        MongoClient.delete(MongoCollections.KEYBOARDS.collectionName, BasicDBObject("_id", keyboardId))
    }

    private fun createButton(button: Button): ObjectId {
        MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
        return button.id
    }

    private fun deleteButton(buttonId: ObjectId) {
        MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", buttonId))
    }

    private fun addButtonToKeyboard(button: ObjectId, keyboard: ObjectId, leadButton: Boolean = false) {
        MongoClient.update(
            MongoCollections.KEYBOARDS.collectionName,
            Keyboard::class.java,
            BasicDBObject("_id", keyboard),
            if (leadButton)
                BasicDBObject("\$set", BasicDBObject("lead_button", button))
            else
                BasicDBObject("\$push", BasicDBObject("buttons", button))
        )
    }

    private fun deleteButtonFromKeyboard(button: ObjectId, keyboard: ObjectId, leadButton: Boolean = false) {
        MongoClient.update(
            MongoCollections.KEYBOARDS.collectionName,
            Keyboard::class.java,
            BasicDBObject("_id", keyboard),
            if (leadButton)
                BasicDBObject("\$unset", BasicDBObject("lead_button", BsonNull()))
            else
                BasicDBObject("\$pull", BasicDBObject("buttons", button))
        )
    }

    private fun addPayload(payload: Payload): ObjectId {
        MongoClient.create(MongoCollections.PAYLOADS.collectionName, payload, Payload::class.java)
        return payload.id
    }

    private fun deletePayload(payloadId: ObjectId) {
        MongoClient.delete(MongoCollections.PAYLOADS.collectionName, BasicDBObject("_id", payloadId))
    }
}
