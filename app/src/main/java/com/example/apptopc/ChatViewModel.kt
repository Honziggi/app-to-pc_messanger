package com.example.apptopc

import androidx.compose.runtime.mutableStateListOf
import com.example.apptopc.data.ChatMessage

object ChatViewModel {
    val messages = mutableStateListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
    }
}


