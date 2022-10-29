package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import org.bson.types.ObjectId
import server.models.Result

data class LinkButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String
) : Request() {

    override val successMessage: String
        get() = "Button with ID \"$buttonId\" successfully linked to $type with ID \"$link\""

    override fun validateData(): Result? {
        RequestValidator.validateIds(buttonId, link)?.let { return it }
        RequestValidator.validateButtonExistence(buttonId)?.let { return it }
        RequestValidator.validateResourceExistence(type, link)?.let { return it }
        if (type == "keyboard") {
            DataManager.getKeyboards()
                .filter { it.buttons.contains(ObjectId(buttonId)) }
                .forEach { keyboard -> RequestValidator.validateLoopLinking(link, keyboard.id.toHexString())?.let { return it } }
        }
        return null
    }
}