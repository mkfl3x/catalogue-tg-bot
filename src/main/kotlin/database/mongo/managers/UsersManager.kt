package database.mongo.managers

import database.mongo.MongoCollections
import database.mongo.collections.MongoCollection
import database.mongo.models.users.User

object UsersManager {

    private val users = MongoCollection(MongoCollections.USERS, User::class.java)

    fun getUser(name: String): User? {
        users.reload()
        return users.entities.firstOrNull { it.username == name }
    }
}