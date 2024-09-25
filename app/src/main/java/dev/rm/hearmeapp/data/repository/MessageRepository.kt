package dev.rm.hearmeapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.rm.hearmeapp.data.model.Message

class MessageRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("messages")

    fun saveMessage(
        text: String,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val message = Message(userId = userId, text = text)
            database.push().setValue(message).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception)
                }
            }
        } else {
            onComplete(false, Exception("User not authenticated"))
        }
    }
}