package database.mongo.managers

import database.mongo.MongoCollections
import database.mongo.MongoNullDataException
import database.mongo.collections.MongoCollection
import database.mongo.models.users.User

object UsersManager {

    private val users = MongoCollection(MongoCollections.USERS, User::class.java)

    fun getUser(username: String) = users.entities.firstOrNull { it.username == username }
        ?: throw MongoNullDataException("User '$username' doesn't exist")
}