package dev.rm.hearmeapp.data.model

data class Message(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val date: Long = System.currentTimeMillis()
)