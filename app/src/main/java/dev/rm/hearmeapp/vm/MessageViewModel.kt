package dev.rm.hearmeapp.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.rm.hearmeapp.data.repository.MessageRepository

class MessageViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

    private val _messageState = MutableLiveData<MessageState>()
    val messageState: LiveData<MessageState> = _messageState

    fun saveMessage(text: String) {
        _messageState.value = MessageState.Loading
        messageRepository.saveMessage(text) { success, error ->
            if (success) {
                _messageState.value = MessageState.SavedToDB
            } else {
                _messageState.value = MessageState.Error(error?.message ?: "Error saving message")
            }
        }
    }
}

sealed class MessageState {
    object Loading : MessageState()
    object SavedToDB : MessageState()
    data class Error(val message: String) : MessageState()
}