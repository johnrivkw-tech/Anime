package com.example.animetracker.data.network

/**
 * Data models for calls to Google's Gemini API (generateContent), used to
 * power the "AI Picks For You" home section. Requests carry a single text
 * prompt; [GeminiGenerationConfig.responseMimeType] of "application/json"
 * asks Gemini to skip markdown fences and reply with raw JSON, which keeps
 * parsing in [GeminiRepository] simple.
 */

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)

data class GeminiPart(val text: String)

data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json"
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(val content: GeminiContent?)

data class GeminiError(val message: String? = null)

/** A single recommendation Gemini returns, parsed out of its JSON reply. */
data class GeminiRecommendation(
    val title: String,
    val reason: String,
    val genres: List<String> = emptyList()
)

/**
 * Shape of a chat turn's reply: conversational text plus zero or more
 * structured recommendations the UI can render as poster chips underneath
 * the message bubble.
 */
data class GeminiChatReply(
    val reply: String,
    val recommendations: List<GeminiRecommendation> = emptyList()
)
