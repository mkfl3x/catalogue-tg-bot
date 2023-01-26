package database.mongo

enum class MongoCollections(val collectionName: String) {
    KEYBOARDS("keyboards"),
    BUTTONS("buttons"),
    PAYLOADS("payloads"),
    USERS("users")
}