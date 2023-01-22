package server.models.requests.data

import com.google.gson.annotations.SerializedName
import database.mongo.managers.DataManager
import org.bson.types.ObjectId
import server.RequestActions.linkButton
import server.models.requests.Request
import server.validations.RequestValidator

data class LinkButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/link_button_request.json"

    override fun validateData() {
        RequestValidator.validateIds(buttonId, link)
        RequestValidator.validateButtonExistence(buttonId)
        RequestValidator.validateResourceExistence(type, link)
        if (type == "keyboard") {
            DataManager.getKeyboards()
                .filter { it.buttons.contains(ObjectId(buttonId)) }
                .forEach { keyboard ->
                    RequestValidator.validateLoopLinking(link, keyboard.id.toHexString())
                }
        }
    }

    override fun relatedAction() = linkButton(this)
}