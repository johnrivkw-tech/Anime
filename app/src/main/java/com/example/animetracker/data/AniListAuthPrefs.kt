package com.example.animetracker.data

import android.content.Context

/**
 * SharedPreferences-backed store for the signed-in AniList session: the
 * OAuth access token plus the small bit of profile info shown in Settings.
 * Mirrors the shape of [AppSettingsPrefs] / [ProfilePrefs].
 *
 * The token comes from AniList's *implicit grant* flow, so it's a
 * long-lived (typically ~1 year) bearer token handed straight back in the
 * OAuth redirect — there's no refresh token and no client secret involved.
 *
 * Note: like the rest of this app's prefs classes, this uses plain
 * SharedPreferences rather than EncryptedSharedPreferences. That's fine for
 * a hobby project, but if you're shipping this more broadly, consider
 * swapping to `androidx.security:security-crypto`'s EncryptedSharedPreferences
 * so the access token isn't stored in cleartext on disk.
 */
class AniListAuthPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("anilist_auth_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (expiresAt != 0L && System.currentTimeMillis() >= expiresAt) return null
        return token
    }

    fun getUserId(): Int? = prefs.getInt(KEY_USER_ID, -1).takeIf { it != -1 }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)

    fun getLastSyncedAtMillis(): Long? = prefs.getLong(KEY_LAST_SYNCED_AT, 0L).takeIf { it != 0L }

    /** Saves the token from the OAuth redirect. [expiresInSeconds] is AniList's `expires_in`. */
    fun saveSession(accessToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun saveProfile(userId: Int, username: String, avatarUrl: String?) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .apply()
    }

    fun saveLastSyncedAtNow() {
        prefs.edit().putLong(KEY_LAST_SYNCED_AT, System.currentTimeMillis()).apply()
    }

    /** Signs out: clears the token and cached profile. Local library data is untouched. */
    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_EXPIRES_AT = "expires_at_millis"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_LAST_SYNCED_AT = "last_synced_at_millis"
    }
}
