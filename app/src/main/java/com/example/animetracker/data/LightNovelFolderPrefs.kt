package com.example.animetracker.data

import android.content.Context

/**
 * Remembers the single SAF (Storage Access Framework) folder the user has
 * linked for light novels, so it survives app restarts. We only ever store
 * the tree URI string here — the actual permission grant is persisted
 * separately via ContentResolver.takePersistableUriPermission, and the
 * folder's contents are re-scanned live each time rather than cached.
 */
class LightNovelFolderPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("light_novel_folder_prefs", Context.MODE_PRIVATE)

    fun getFolderUri(): String? = prefs.getString(KEY_FOLDER_URI, null)

    fun setFolderUri(uri: String?) {
        prefs.edit().putString(KEY_FOLDER_URI, uri).apply()
    }

    companion object {
        private const val KEY_FOLDER_URI = "folder_uri"
    }
}
