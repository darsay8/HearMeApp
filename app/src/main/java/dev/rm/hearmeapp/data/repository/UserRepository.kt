package dev.rm.hearmeapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.rm.hearmeapp.data.model.User
import org.mindrot.jbcrypt.BCrypt

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun registerUser(
        username: String,
        email: String,
        password: String,
        onComplete: (FirebaseUser?, Exception?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates =
                    UserProfileChangeRequest.Builder().setDisplayName(username).build()
                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileUpdateTask ->
                    if (profileUpdateTask.isSuccessful) {
                        saveUserToDatabase(
                            User(username, email, hashPassword(password)),
                            onComplete
                        )
                    } else {
                        onComplete(null, profileUpdateTask.exception)
                    }
                }
            } else {
                onComplete(null, task.exception)
            }
        }
    }

    private fun saveUserToDatabase(user: User, onComplete: (FirebaseUser?, Exception?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(auth.currentUser, null)
            } else {
                onComplete(null, task.exception)
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onComplete: (FirebaseUser?, Exception?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(auth.currentUser, null)
            } else {
                onComplete(null, task.exception)
            }
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}