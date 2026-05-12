package com.example.instant

import androidx.compose.runtime.mutableStateListOf

object ChatViewModel {
    val messages = mutableStateListOf<String>()

    fun addMessage(message: String) {
        messages.add(message)
    }
}