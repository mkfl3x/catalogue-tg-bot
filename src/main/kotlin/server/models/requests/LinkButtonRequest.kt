package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result

data class LinkButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String
) : Request() {

    override val schema: Schemas
        get() = Schemas.LINK_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Button with ID \"$buttonId\" successfully linked to $type with ID \"$link\""

    override fun validateData(): Result? {
        RequestValidator.validateIds(buttonId, link)?.let { return it }
        if (DataManager.getButton(buttonId) == null)
            return Result.error(Error.BUTTON_DOES_NOT_EXIST, buttonId)
        RequestValidator.validateResourceExistence(type, link)?.let { return it }
        if (type == "keyboard" && DataManager.getKeyboard(link)!!.leadButton != null)
            return Result.error(Error.KEYBOARD_ALREADY_LINKED, link)
        return null
    }
}