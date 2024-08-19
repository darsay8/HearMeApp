package dev.rm.hearmeapp.data.repository

import dev.rm.hearmeapp.data.model.User

object UserRepository {
    private val users = mutableListOf<User>()
    private var currentUser: User? = null

    init {
        users.add(User("user", "user@mail.com", "password123"))
        users.add(User("john_doe", "john@example.com", "password123"))
        users.add(User("jane_smith", "jane@example.com", "securePass456"))
        users.add(User("alice_johnson", "alice@example.com", "alicePass789"))
        users.add(User("bob_brown", "bob@example.com", "bobPassword101"))
        users.add(User("charlie_davis", "charlie@example.com", "charlie123"))
    }

    fun registerUser(user: User): Boolean {

        if (users.any { it.username == user.username || it.email == user.email }) {
            return false
        }
        users.add(user)
        return true
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val user = users.find { it.username == username && it.password == password }
        currentUser = user
        return user != null
    }

    fun getCurrentUser(): User? = currentUser

    fun clearSession() {
        currentUser = null
    }
}