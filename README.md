# Rei

A personal anime, manga, and light novel tracker built with Kotlin, Jetpack
Compose, Room, and MVVM — with a home feed, schedule, AI chat, and a couple
of just-for-fun extras thrown in.

## What's included

**Tracking**
- Add, edit, and delete anime in your list, with episodes-watched progress
  and an optional total-episode count
- **Status**: Watching / Completed / Plan to Watch, settable per title and
  filterable from My List
- **Rating**: tap-to-set 1–10 star rating
- Search bar that filters your list by name as you type (combines with the
  status filter)
- Import/export your list as a MyAnimeList-compatible XML file
- Manga chapter tracking and a built-in manga reader
- Light novel folder support with a PDF reader
- Data is stored locally with Room and survives app restarts

**Discovery**
- Home feed pulling live data from AniList and Jikan (MyAnimeList's API) —
  trending titles, seasonal releases, and more
- A schedule screen for upcoming episode air dates
- Search screen for looking up new titles to add to your list
- Manga lookup via MangaDex

**Extras**
- AI chat screen (Gemini-backed) for talking through recommendations
- A couple of lightweight mini-games (a gacha screen, a card game)
- Profile screen with stats, badges, and rank tiers
- Material 3 UI with automatic light/dark theme, Android 12+ dynamic color,
  and a few selectable app themes

## How to open it

1. Unzip this folder somewhere on your computer.
2. Open **Android Studio** → File → Open → select the `Rei` (or `Anime-main`)
   folder.
3. Let Gradle sync (first sync downloads dependencies, so it needs internet).
4. Click Run ▶ on an emulator or physical device (minSdk 26 / targetSdk 36).

**Note on the Gradle wrapper:** this project doesn't include the
`gradle-wrapper.jar` binary (it can't be generated outside of Gradle/Android
Studio itself). Opening the folder in Android Studio is all you need — it
will configure Gradle automatically on first sync. If you ever want to build
from the command line, run `gradle wrapper` once (with Gradle installed) to
regenerate the wrapper files.

**API keys:** the AniList/Jikan/MangaDex screens hit public APIs and need no
key. The AI chat screen talks to the Gemini API and expects a key to be
supplied via `GeminiApiService`/`GeminiRepository` — see those files for
where to plug one in.

## Project structure

```
app/src/main/java/com/example/animetracker/
├── MainActivity.kt              # Single activity, hosts the Compose UI + nav graph
├── data/
│   ├── Anime.kt, AnimeStatus.kt, AnimeDao.kt, AnimeDatabase.kt, AnimeRepository.kt
│   ├── MangaEntity.kt, MangaDao.kt, MangaRepository.kt
│   ├── LightNovelEntity.kt, LightNovelDao.kt, LightNovelRepository.kt
│   ├── ChatMessageEntity.kt, ChatDao.kt, ChatRepository.kt
│   ├── *Prefs.kt                # DataStore-backed settings (theme, favorites, filters, etc.)
│   ├── MalXmlPort.kt            # MyAnimeList XML import/export
│   ├── Converters.kt            # Room TypeConverters
│   └── network/                 # AniList, Jikan, MangaDex, Gemini API services + repos
├── viewmodel/
│   └── AnimeViewModel.kt        # UI state, search/filter logic, actions
└── ui/
    ├── theme/                   # Color.kt, Theme.kt, Type.kt, Fonts.kt, AppThemeOption.kt
    ├── navigation/               # Destination.kt (bottom-nav routes), BottomNavBar.kt
    ├── model/                   # UI-facing data models (home cards, badges, chat, gacha, etc.)
    ├── components/               # AnimeCard, AnimeGridCard, ReiWordmark, FeaturedBanner, ...
    └── screens/
        ├── HomeFeedScreen.kt     # Discovery feed (AniList/Jikan trending + seasonal)
        ├── HomeScreen.kt         # My List: search, status filter tabs, grid
        ├── ScheduleScreen.kt     # Upcoming air dates
        ├── SearchScreen.kt / SearchAnimeDialog.kt
        ├── AnimeDetailsScreen.kt / AddEditAnimeDialog.kt
        ├── MangaChaptersScreen.kt / MangaReaderScreen.kt
        ├── LightNovelsScreen.kt
        ├── AiChatScreen.kt       # Gemini-backed chat
        ├── ProfileScreen.kt
        ├── SettingsScreen.kt
        ├── GameScreen.kt / OnePieceGachaScreen.kt
        └── SplashScreen.kt
```

The package name is still `com.example.animetracker` and the app ID matches
it — the app itself is branded **Rei**, but the underlying package wasn't
renamed (that's a much larger refactor touching every file plus
`applicationId`/`namespace` in `app/build.gradle.kts`). Rename it yourself in
Android Studio via Right-click package → Refactor → Rename if you want the
two to match before publishing anywhere.

## Ideas for what to build next

- **Sorting** — by name, rating, or last updated, via a menu in the top bar.
- **Genres/tags** — a list column on `Anime` plus chips on the card.
- **Room migrations** — schema changes currently fall back to destructive
  migration; add real `Migration` objects if you want future updates to
  preserve existing data.
- **Offline caching** for the AniList/Jikan home feed, so it's not blank
  without a connection.
