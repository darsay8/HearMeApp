package dev.rm.hearmeapp.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rm.hearmeapp.data.model.Message
import dev.rm.hearmeapp.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

    private val _messageState = MutableStateFlow<MessageState>(MessageState.Loading)
    val messageState: StateFlow<MessageState> = _messageState

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun saveMessage(text: String) {
        _messageState.value = MessageState.Loading
        viewModelScope.launch {
            try {
                val success = messageRepository.saveMessage(text)
                _messageState.value = if (success) {
                    MessageState.SavedToDB
                } else {
                    MessageState.Error("Error saving message")
                }
            } catch (e: Exception) {
                _messageState.value = MessageState.Error(e.message ?: "Error saving message")
            }
        }
    }

    fun getMessages() {
        _messageState.value = MessageState.Loading
        viewModelScope.launch {
            try {
                val messages = messageRepository.getMessages()
                _messages.value = messages.values.toList()

                _messageState.value = if (_messages.value.isEmpty()) {
                    MessageState.Error("No messages found")
                } else {
                    MessageState.MessagesRetrieved(_messages.value)
                }
            } catch (e: Exception) {
                _messageState.value = MessageState.Error(e.message ?: "Error reading messages")
            }
        }
    }

    fun getMessageById(messageId: String) {
        viewModelScope.launch {
            try {
                val message = messageRepository.getMessageById(messageId)
                _messageState.value = MessageState.MessageRetrieved(message)
            } catch (e: Exception) {
                _messageState.value = MessageState.Error(e.message ?: "Error retrieving message")
            }
        }
    }

    fun updateMessage(messageId: String, newText: String) {
        _messageState.value = MessageState.Loading
        viewModelScope.launch {
            try {
                val success = messageRepository.updateMessage(messageId, newText)
                _messageState.value = if (success) {
                    MessageState.SavedToDB
                } else {
                    MessageState.Error("Error updating message")
                }
            } catch (e: Exception) {
                _messageState.value = MessageState.Error(e.message ?: "Error updating message")
            }
        }
    }

    fun deleteMessage(messageId: String) {
        _messageState.value = MessageState.Loading
        viewModelScope.launch {
            try {
                val success = messageRepository.deleteMessage(messageId)
                if (success) {
                    _messages.value = _messages.value.filter { it.id != messageId }
                    _messageState.value = MessageState.Deleted
                } else {
                    _messageState.value = MessageState.Error("Error deleting message")
                }
            } catch (e: Exception) {
                _messageState.value = MessageState.Error(e.message ?: "Error deleting message")
            }
        }
    }
}

sealed class MessageState {
    object Loading : MessageState()
    object SavedToDB : MessageState()
    object Deleted : MessageState()
    data class Error(val message: String) : MessageState()
    data class MessageRetrieved(val message: Message?) : MessageState()
    data class MessagesRetrieved(val messages: List<Message>) :
        MessageState()
}