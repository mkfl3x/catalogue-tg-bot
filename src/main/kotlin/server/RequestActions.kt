package server

import bot.context.KeyboardStates
import com.mongodb.BasicDBObject
import database.mongo.DataManager
import database.mongo.MongoClient
import database.mongo.MongoCollections
import database.mongo.models.Button
import database.mongo.models.Keyboard
import database.mongo.models.Payload
import org.bson.types.ObjectId
import server.models.RequestAction
import server.models.Response
import server.models.ResponseStatus
import server.models.requests.*

object RequestActions {

    fun addKeyboard(request: AddKeyboardRequest): Response {
        with(request) {
            // Create keyboard
            val keyboard = Keyboard(ObjectId(), name, emptyList(), emptyList())
            MongoClient.create(MongoCollections.KEYBOARDS.collectionName, keyboard, Keyboard::class.java)
            // Create button leads to new keyboard (if keyboard creating as not detached)
            location?.let {
                // Create button
                val button = Button(ObjectId(), it.leadButtonText, "keyboard", keyboard.id)
                MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
                // Add button on host keyboard (it leads to new keyboard)
                addButtonToKeyboard(button.id, ObjectId(it.hostKeyboard))
                // Add button to new keyboard lead_buttons
                addButtonToKeyboard(button.id, keyboard.id, leadButtons = true)
            }
            return Response(ResponseStatus.SUCCESS, RequestAction.CREATE, keyboard.id)
        }
    }

    fun addButton(request: AddButtonRequest): Response {
        with(request) {
            // Create button
            val button = Button(ObjectId(), text, type, ObjectId(link))
            MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
            // Add button to host keyboard (if button creating as not detached)
            // TODO: maybe make sense set hostKeyboard as necessary
            hostKeyboard?.let { addButtonToKeyboard(button.id, ObjectId(hostKeyboard)) }
            // If button leads to keyboard then add button to host keyboard's lead_buttons
            if (button.type == "keyboard")
                addButtonToKeyboard(button.id, ObjectId(link), leadButtons = true)
            return Response(ResponseStatus.SUCCESS, RequestAction.CREATE, button.id)
        }
    }

    fun addPayload(request: AddPayloadRequest): Response {
        with(request) {
            // Create payload
            val payload = Payload(ObjectId(), name, type, data)
            MongoClient.create(MongoCollections.PAYLOADS.collectionName, payload, Payload::class.java)
            // Add button leads to new payload (if payload creating as not detached)
            location?.let {
                // Create button
                val button = Button(ObjectId(), it.leadButtonText, "payload", payload.id)
                MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
                // Add button to host keyboard
                addButtonToKeyboard(button.id, ObjectId(it.hostKeyboard))
            }
            return Response(ResponseStatus.SUCCESS, RequestAction.CREATE, payload.id)
        }
    }

    fun deleteKeyboard(keyboard: Keyboard, detachOnly: Boolean): Response {
        with(keyboard) {
            // Delete keyboard from states
            // TODO: add mechanism for update user's keyboards
            KeyboardStates.deleteKeyboard(id.toHexString())
            // Delete lead buttons (if keyboard is not detached)
            leadButtons.forEach { leadButton ->
                // Delete buttons leads to deleting keyboard from all keyboards
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.buttons.contains(leadButton) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(leadButton, keyboard.id) }
                // Delete button leads to deleting keyboard
                MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", leadButton))
                // Delete button from lead_buttons of deleting keyboard (if deleting keyboard is not detached)
                if (leadButtons.contains(leadButton))
                    deleteButtonFromKeyboard(leadButton, id, leadButtons = true)
            }
            // Delete keyboard from collection
            if (detachOnly.not()) {
                MongoClient.delete(MongoCollections.KEYBOARDS.collectionName, BasicDBObject("_id", id))
                return Response(ResponseStatus.SUCCESS, RequestAction.DELETE, id)
            }
            return Response(ResponseStatus.SUCCESS, RequestAction.DETACH, id)
        }
    }

    fun deleteButton(request: DeleteButtonRequest): Response {
        with(request) {
            // Get button from
            val button = DataManager.getButton(buttonId)!!
            // If button leads to keyboard then detach keyboard
            DataManager.getKeyboards()
                .find { keyboard -> keyboard.leadButtons.contains(button.id) }
                ?.let { deleteButtonFromKeyboard(button.id, it.id, leadButtons = true) }
            // Delete button
            // TODO: add mechanism for update user's keyboards
            MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", buttonId))
            // Delete button from all keyboards
            DataManager.getKeyboards()
                .filter { keyboard -> keyboard.buttons.contains(button.id) }
                .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
            return Response(ResponseStatus.SUCCESS, RequestAction.DELETE, button.id)
        }
    }

    fun deletePayload(request: DeletePayloadRequest): Response {
        with(request) {
            // Get payload
            val payload = DataManager.getPayload(payloadId)!!

            // Delete payload from collection
            MongoClient.delete(MongoCollections.PAYLOADS.collectionName, BasicDBObject("_id", payload.id))

            // Delete all buttons leads to deleting payload from keyboards
            DataManager.getButtons()
                .filter { button -> button.linkTo == payload.id }
                .forEach { button ->
                    // Delete buttons from keyboards
                    DataManager.getKeyboards()
                        .filter { keyboard -> keyboard.buttons.contains(button.id) }
                        .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
                    // Delete buttons
                    MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", button.id))
                }
            return Response(ResponseStatus.SUCCESS, RequestAction.DELETE, payload.id)
        }
    }

    fun linkButton(request: LinkButtonRequest): Response {
        with(request) {
            // If button leading to keyboard then remove this button from lead_buttons
            val button = DataManager.getButton(request.buttonId)!!
            if (button.type == "keyboard")
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.leadButtons.contains(button.id) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id, leadButtons = true) }
            // If linking to keyboard then add button to lead_buttons of keyboard
            if (request.type == "keyboard")
                addButtonToKeyboard(ObjectId(request.buttonId), ObjectId(request.link), leadButtons = true)
            // Update button's link and link type
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
            return Response(ResponseStatus.SUCCESS, RequestAction.LINK, button.id)
        }
    }

    private fun addButtonToKeyboard(button: ObjectId, keyboard: ObjectId, leadButtons: Boolean = false) {
        MongoClient.update(
            MongoCollections.KEYBOARDS.collectionName,
            Keyboard::class.java,
            BasicDBObject("_id", keyboard),
            BasicDBObject("\$push", BasicDBObject(if (leadButtons) "lead_buttons" else "buttons", button))
        )
    }

    private fun deleteButtonFromKeyboard(button: ObjectId, keyboard: ObjectId, leadButtons: Boolean = false) {
        MongoClient.update(
            MongoCollections.KEYBOARDS.collectionName,
            Keyboard::class.java,
            BasicDBObject("_id", keyboard),
            BasicDBObject("\$pull", BasicDBObject(if (leadButtons) "lead_buttons" else "buttons", button))
        )
    }
}