package database

enum class MongoCollections(val collectionName: String) {
    KEYBOARDS("keyboards"),
    BUTTONS("buttons"),
    PAYLOADS("payloads")
}