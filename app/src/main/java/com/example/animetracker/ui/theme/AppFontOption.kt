package com.example.animetracker.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.animetracker.R

private val appFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun googleFontFamily(name: String): FontFamily =
    FontFamily(Font(googleFont = GoogleFont(name), fontProvider = appFontProvider))

/**
 * The typeface used across the app's five main tabs (Home, Schedule, My
 * List, Search, Settings). Swapped in at the MaterialTheme.Typography
 * level in [AnimeTrackerTheme] — every Text() that reads a theme style
 * (which is nearly all of them) picks it up automatically, so there's no
 * per-screen wiring to keep in sync. [Default] leaves the system font in
 * place; everything else pulls from Google Fonts through the same
 * provider already used for the "Rei" wordmark and splash screen.
 */
enum class AppFontOption(
    val displayName: String,
    val description: String,
    val fontFamily: FontFamily?
) {
    Default("Default", "The system's built-in font", null),
    Inter("Inter", "Clean, modern, easy to scan", googleFontFamily("Inter")),
    Poppins("Poppins", "Geometric and friendly", googleFontFamily("Poppins")),
    Nunito("Nunito", "Soft and rounded", googleFontFamily("Nunito")),
    Montserrat("Montserrat", "Bold and confident", googleFontFamily("Montserrat")),
    RobotoSlab("Roboto Slab", "A serif with some real weight", googleFontFamily("Roboto Slab")),
    SpaceMono("Space Mono", "Monospace, technical feel", googleFontFamily("Space Mono"))
}
