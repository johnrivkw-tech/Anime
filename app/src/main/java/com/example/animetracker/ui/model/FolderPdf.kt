package com.example.animetracker.ui.model

/** One PDF found by live-scanning the user's linked folder (not stored in Room — re-scanned each time). */
data class FolderPdf(
    val title: String,
    val uri: String
)
