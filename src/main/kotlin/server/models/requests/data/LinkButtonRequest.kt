package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.linkButton
import server.models.requests.Request
import server.validations.RequestDataValidators

data class LinkButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/link_button_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(buttonId, link)
        RequestDataValidators.validateButtonExists(buttonId)
        RequestDataValidators.validateLinkingResourceExistence(type, link)
        if (type == "keyboard") RequestDataValidators.validateLoopButton(link, buttonId)
    }

    override fun relatedAction() = linkButton(this)
}