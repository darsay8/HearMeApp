package dev.rm.hearmeapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.rm.hearmeapp.data.model.Message
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("messages")

    suspend fun saveMessage(text: String): Boolean {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val messageId = database.push().key ?: throw Exception("Could not create message ID")
        val message = Message(id = messageId, userId = userId, text = text)

        return try {
            database.child(messageId).setValue(message).await()
            true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getMessages(): Map<String, Message> {
        val snapshot = database.get().await()
        val messagesMap = mutableMapOf<String, Message>()
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        snapshot.children.forEach { childSnapshot ->
            val message = childSnapshot.getValue(Message::class.java)
            if (message != null) {
                val key = childSnapshot.key
                if (key != null && message.userId == userId) {
                    messagesMap[key] = message
                }
            }
        }
        return messagesMap
    }

    suspend fun getMessageById(messageId: String): Message? {
        return try {
            val snapshot = database.child(messageId).get().await()
            snapshot.getValue(Message::class.java)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateMessage(messageId: String, newText: String): Boolean {
        return try {
            database.child(messageId).child("text").setValue(newText).await()
            true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteMessage(messageId: String): Boolean {
        return try {
            database.child(messageId).removeValue().await()
            true
        } catch (e: Exception) {
            throw e
        }
    }
}