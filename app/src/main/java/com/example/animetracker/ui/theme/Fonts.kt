package com.example.animetracker.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.animetracker.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val pacifico = GoogleFont(name = "Pacifico")

/** Cursive wordmark font, used only for the "Vizora" brand overlay. */
val VizoraLogoFont = FontFamily(
    Font(googleFont = pacifico, fontProvider = fontProvider)
)

private val inter = GoogleFont(name = "Inter")

/** Clean modern sans used for the splash screen's "Loading..." label. */
val InterFontFamily = FontFamily(
    Font(googleFont = inter, fontProvider = fontProvider)
)
