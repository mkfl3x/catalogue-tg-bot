package server.models.requests

import server.models.responses.Response
import server.validations.RequestValidationException
import server.validations.SchemaValidator
import utils.GsonMapper

interface Request {

    val schemaPath: String

    @Throws(RequestValidationException::class)
    fun validateSchema() {
        SchemaValidator.validate(GsonMapper.serialize(this), schemaPath)
    }

    @Throws(RequestValidationException::class)
    fun validateData()

    fun relatedAction(): Response
}