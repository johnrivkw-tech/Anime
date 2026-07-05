package com.example.animetracker.ui.model

/**
 * A single turn in the AI recs chat. [recommendations], if non-empty, are
 * rendered as poster chips underneath the message bubble so the user can
 * tap straight through to a title Gemini just suggested.
 */
data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val recommendations: List<HomeCardItem> = emptyList()
)
