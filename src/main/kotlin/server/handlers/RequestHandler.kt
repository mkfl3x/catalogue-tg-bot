package server.handlers

interface RequestHandler {

    val commonError: String
        get() = "Something went wrong \uD83E\uDD72"
}