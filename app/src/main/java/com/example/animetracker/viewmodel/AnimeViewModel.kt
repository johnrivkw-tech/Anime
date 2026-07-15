package com.example.animetracker.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animetracker.data.Anime
import com.example.animetracker.data.AnimeDatabase
import com.example.animetracker.data.AnimeRepository
import com.example.animetracker.data.AnimeStatus
import com.example.animetracker.data.ChatRepository
import com.example.animetracker.data.LightNovelEntity
import com.example.animetracker.data.LightNovelFolderPrefs
import com.example.animetracker.data.LightNovelRepository
import com.example.animetracker.data.MangaEntity
import com.example.animetracker.data.MangaRepository
import com.example.animetracker.data.ProfilePrefs
import com.example.animetracker.data.ThemePrefs
import com.example.animetracker.ui.theme.AppThemeOption
import com.example.animetracker.data.network.AniListAiringSchedule
import com.example.animetracker.data.network.AniListCharacterEdge
import com.example.animetracker.data.network.AniListMedia
import com.example.animetracker.data.network.AniListRepository
import com.example.animetracker.data.network.GeminiChatRepository
import com.example.animetracker.data.network.GeminiRepository
import com.example.animetracker.data.network.MangaDexChapter
import com.example.animetracker.data.network.MangaDexManga
import com.example.animetracker.data.network.MangaDexRepository
import com.example.animetracker.ui.model.ChatMessage
import com.example.animetracker.ui.model.FolderPdf
import com.example.animetracker.ui.model.GenreCount
import com.example.animetracker.ui.model.HomeCardItem
import com.example.animetracker.ui.model.ProfileStats
import com.example.animetracker.ui.model.toHomeCardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.io.FileOutputStream

class AnimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnimeRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var lightNovelRepository: LightNovelRepository
    private lateinit var mangaRepository: MangaRepository
    private val aniListRepository = AniListRepository()
    private val mangaDexRepository = MangaDexRepository()
    private val geminiRepository = GeminiRepository()
    private val geminiChatRepository = GeminiChatRepository()
    private val profilePrefs = ProfilePrefs(application)
    private val themePrefs = ThemePrefs(application)
    private val lightNovelFolderPrefs = LightNovelFolderPrefs(application)

    private val _themeOption = MutableStateFlow(themePrefs.getTheme())
    val themeOption: StateFlow<AppThemeOption> = _themeOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<AnimeStatus?>(null)
    val statusFilter: StateFlow<AnimeStatus?> = _statusFilter.asStateFlow()

    val filteredAnime: StateFlow<List<Anime>>
    val allLocalAnime: StateFlow<List<Anime>>
    val localByAniListId: StateFlow<Map<Int, AnimeStatus>>
    val continueTracking: StateFlow<List<Anime>>
    val favoriteAnime: StateFlow<List<HomeCardItem>>

    // --- Online "add anime" search (AniList) ---
    private val _searchResults = MutableStateFlow<List<AniListMedia>>(emptyList())
    val searchResults: StateFlow<List<AniListMedia>> = _searchResults.asStateFlow()

    private val _isSearchingApi = MutableStateFlow(false)
    val isSearchingApi: StateFlow<Boolean> = _isSearchingApi.asStateFlow()

    private val _searchApiError = MutableStateFlow<String?>(null)
    val searchApiError: StateFlow<String?> = _searchApiError.asStateFlow()

    private var searchJob: Job? = null

    // --- Home feed sections (AniList) ---
    private val _trending = MutableStateFlow<List<AniListMedia>>(emptyList())
    val trending: StateFlow<List<AniListMedia>> = _trending.asStateFlow()

    private val _popularThisSeason = MutableStateFlow<List<AniListMedia>>(emptyList())
    val popularThisSeason: StateFlow<List<AniListMedia>> = _popularThisSeason.asStateFlow()

    private val _topRated = MutableStateFlow<List<AniListMedia>>(emptyList())
    val topRated: StateFlow<List<AniListMedia>> = _topRated.asStateFlow()

    private val _newReleases = MutableStateFlow<List<AniListMedia>>(emptyList())
    val newReleases: StateFlow<List<AniListMedia>> = _newReleases.asStateFlow()

    private val _recommended = MutableStateFlow<List<AniListMedia>>(emptyList())
    val recommended: StateFlow<List<AniListMedia>> = _recommended.asStateFlow()

    private val _isHomeFeedLoading = MutableStateFlow(false)
    val isHomeFeedLoading: StateFlow<Boolean> = _isHomeFeedLoading.asStateFlow()

    private val _homeFeedError = MutableStateFlow<String?>(null)
    val homeFeedError: StateFlow<String?> = _homeFeedError.asStateFlow()

    // --- AI picks (Gemini), personalized from the local watchlist ---
    private val _aiRecommendations = MutableStateFlow<List<HomeCardItem>>(emptyList())
    val aiRecommendations: StateFlow<List<HomeCardItem>> = _aiRecommendations.asStateFlow()

    private val _isLoadingAiRecommendations = MutableStateFlow(false)
    val isLoadingAiRecommendations: StateFlow<Boolean> = _isLoadingAiRecommendations.asStateFlow()

    private val _aiRecommendationsError = MutableStateFlow<String?>(null)
    val aiRecommendationsError: StateFlow<String?> = _aiRecommendationsError.asStateFlow()

    // --- AI recs chat ---
    // Backed by Room (via chatRepository) instead of an in-memory list, so
    // the conversation survives app restarts, not just rotation.
    val chatMessages: StateFlow<List<ChatMessage>>

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    // Flips to true once the initial home feed load finishes (success or
    // failure) and stays true afterward, even if loadHomeFeed() is called
    // again later (e.g. a retry button or pull-to-refresh). MainActivity
    // gates the splash screen on this so the app doesn't reveal a half-loaded
    // UI on cold start.
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()

    // --- Anime details screen (AniList) ---
    private val _animeDetails = MutableStateFlow<AniListMedia?>(null)
    val animeDetails: StateFlow<AniListMedia?> = _animeDetails.asStateFlow()

    private val _isDetailsLoading = MutableStateFlow(false)
    val isDetailsLoading: StateFlow<Boolean> = _isDetailsLoading.asStateFlow()

    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError: StateFlow<String?> = _detailsError.asStateFlow()

    private val _characters = MutableStateFlow<List<AniListCharacterEdge>>(emptyList())
    val characters: StateFlow<List<AniListCharacterEdge>> = _characters.asStateFlow()

    // --- Discover tab: browse by genre/season/year (AniList) ---
    private val _discoverGenre = MutableStateFlow<String?>(null)
    val discoverGenre: StateFlow<String?> = _discoverGenre.asStateFlow()

    private val _discoverSeason = MutableStateFlow<String?>(null)
    val discoverSeason: StateFlow<String?> = _discoverSeason.asStateFlow()

    private val _discoverYear = MutableStateFlow<Int?>(null)
    val discoverYear: StateFlow<Int?> = _discoverYear.asStateFlow()

    private val _discoverResults = MutableStateFlow<List<AniListMedia>>(emptyList())
    val discoverResults: StateFlow<List<AniListMedia>> = _discoverResults.asStateFlow()

    private val _isDiscoverLoading = MutableStateFlow(false)
    val isDiscoverLoading: StateFlow<Boolean> = _isDiscoverLoading.asStateFlow()

    private val _discoverError = MutableStateFlow<String?>(null)
    val discoverError: StateFlow<String?> = _discoverError.asStateFlow()

    private var discoverJob: Job? = null

    // --- Search tab: full-catalog search, separate from the "add to list" dialog ---
    private val _catalogQuery = MutableStateFlow("")
    val catalogQuery: StateFlow<String> = _catalogQuery.asStateFlow()

    private val _catalogResults = MutableStateFlow<List<AniListMedia>>(emptyList())
    val catalogResults: StateFlow<List<AniListMedia>> = _catalogResults.asStateFlow()

    private val _isCatalogSearching = MutableStateFlow(false)
    val isCatalogSearching: StateFlow<Boolean> = _isCatalogSearching.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    private var catalogJob: Job? = null

    // --- Profile: banner image + display name (SharedPreferences) ---
    private val _profileBannerPath = MutableStateFlow(profilePrefs.getBannerPath())
    val profileBannerPath: StateFlow<String?> = _profileBannerPath.asStateFlow()

    private val _profileDisplayName = MutableStateFlow(profilePrefs.getDisplayName())
    val profileDisplayName: StateFlow<String> = _profileDisplayName.asStateFlow()

    private val _profileAvatarPath = MutableStateFlow(profilePrefs.getAvatarPath())
    val profileAvatarPath: StateFlow<String?> = _profileAvatarPath.asStateFlow()

    private val _isBannerSaving = MutableStateFlow(false)
    val isBannerSaving: StateFlow<Boolean> = _isBannerSaving.asStateFlow()

    private val _isAvatarSaving = MutableStateFlow(false)
    val isAvatarSaving: StateFlow<Boolean> = _isAvatarSaving.asStateFlow()

    val profileJoinedAtMillis: Long = profilePrefs.getJoinedAtMillis()

    // --- Profile: aggregate watchlist stats (local) ---
    val profileStats: StateFlow<ProfileStats>

    // --- Schedule tab ---
    private val _scheduleDate = MutableStateFlow(LocalDate.now())
    val scheduleDate: StateFlow<LocalDate> = _scheduleDate.asStateFlow()

    private val _scheduleEntries = MutableStateFlow<List<AniListAiringSchedule>>(emptyList())
    val scheduleEntries: StateFlow<List<AniListAiringSchedule>> = _scheduleEntries.asStateFlow()

    private val _isScheduleLoading = MutableStateFlow(false)
    val isScheduleLoading: StateFlow<Boolean> = _isScheduleLoading.asStateFlow()

    private val _scheduleError = MutableStateFlow<String?>(null)
    val scheduleError: StateFlow<String?> = _scheduleError.asStateFlow()

    // --- Light novels tab ---
    val lightNovels: StateFlow<List<LightNovelEntity>>

    private val _linkedFolderUri = MutableStateFlow(lightNovelFolderPrefs.getFolderUri())
    val linkedFolderUri: StateFlow<String?> = _linkedFolderUri.asStateFlow()

    private val _linkedFolderName = MutableStateFlow<String?>(null)
    val linkedFolderName: StateFlow<String?> = _linkedFolderName.asStateFlow()

    private val _folderNovels = MutableStateFlow<List<FolderPdf>>(emptyList())
    val folderNovels: StateFlow<List<FolderPdf>> = _folderNovels.asStateFlow()

    // --- Manga: library (Room-backed) ---
    val mangaLibrary: StateFlow<List<MangaEntity>>

    // --- Manga: search ---
    private val _mangaSearchQuery = MutableStateFlow("")
    val mangaSearchQuery: StateFlow<String> = _mangaSearchQuery.asStateFlow()

    private val _mangaSearchResults = MutableStateFlow<List<MangaDexManga>>(emptyList())
    val mangaSearchResults: StateFlow<List<MangaDexManga>> = _mangaSearchResults.asStateFlow()

    private val _isMangaSearchLoading = MutableStateFlow(false)
    val isMangaSearchLoading: StateFlow<Boolean> = _isMangaSearchLoading.asStateFlow()

    private val _mangaSearchError = MutableStateFlow<String?>(null)
    val mangaSearchError: StateFlow<String?> = _mangaSearchError.asStateFlow()

    // --- Manga: chapters for whichever manga was last tapped ---
    private val _selectedMangaTitle = MutableStateFlow<String?>(null)
    val selectedMangaTitle: StateFlow<String?> = _selectedMangaTitle.asStateFlow()

    private val _mangaChapters = MutableStateFlow<List<MangaDexChapter>>(emptyList())
    val mangaChapters: StateFlow<List<MangaDexChapter>> = _mangaChapters.asStateFlow()

    private val _isChaptersLoading = MutableStateFlow(false)
    val isChaptersLoading: StateFlow<Boolean> = _isChaptersLoading.asStateFlow()

    private val _chaptersError = MutableStateFlow<String?>(null)
    val chaptersError: StateFlow<String?> = _chaptersError.asStateFlow()

    // --- Manga: pages for whichever chapter was last opened ---
    private val _chapterPages = MutableStateFlow<List<String>>(emptyList())
    val chapterPages: StateFlow<List<String>> = _chapterPages.asStateFlow()

    private val _isPagesLoading = MutableStateFlow(false)
    val isPagesLoading: StateFlow<Boolean> = _isPagesLoading.asStateFlow()

    private val _pagesError = MutableStateFlow<String?>(null)
    val pagesError: StateFlow<String?> = _pagesError.asStateFlow()

    init {
        val database = AnimeDatabase.getDatabase(application)
        repository = AnimeRepository(database.animeDao())
        chatRepository = ChatRepository(database.chatDao())
        lightNovelRepository = LightNovelRepository(database.lightNovelDao())
        mangaRepository = MangaRepository(database.mangaDao())

        lightNovels = lightNovelRepository.allNovels.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        mangaLibrary = mangaRepository.allManga.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        chatMessages = chatRepository.messages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        filteredAnime = combine(
            repository.allAnime,
            _searchQuery,
            _statusFilter
        ) { list, query, statusFilter ->
            list.filter { anime ->
                val matchesQuery = query.isBlank() ||
                    anime.name.contains(query, ignoreCase = true)
                val matchesStatus = statusFilter == null || anime.status == statusFilter
                matchesQuery && matchesStatus
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        allLocalAnime = repository.allAnime.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        localByAniListId = repository.allAnime
            .map { list ->
                list.mapNotNull { anime -> anime.aniListId?.let { it to anime.status } }.toMap()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

        continueTracking = repository.allAnime
            .map { list -> list.filter { it.status == AnimeStatus.WATCHING } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

        favoriteAnime = repository.allAnime
            .map { list -> list.filter { it.isFavorite }.map { it.toHomeCardItem() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

        profileStats = combine(
            repository.allAnime,
            mangaRepository.allManga,
            lightNovelRepository.allNovels
        ) { list, manga, novels ->
            // AniList's per-episode runtime when we have it; a flat
            // 24 min/episode (typical TV anime) for entries that don't.
            val totalMinutes = list.sumOf { anime ->
                val perEpisode = (anime.episodeDurationMinutes ?: 24).toLong()
                perEpisode * anime.episodesWatched
            }
            val rated = list.filter { it.rating > 0 }
            val genreCounts = list
                .flatMap { it.genres }
                .groupingBy { it }
                .eachCount()
                .map { (genre, count) -> GenreCount(genre, count) }
                .sortedByDescending { it.count }
                .take(5)
            ProfileStats(
                totalAnime = list.size,
                completed = list.count { it.status == AnimeStatus.COMPLETED },
                watching = list.count { it.status == AnimeStatus.WATCHING },
                planToWatch = list.count { it.status == AnimeStatus.PLAN_TO_WATCH },
                favorites = list.count { it.isFavorite },
                totalWatchMinutes = totalMinutes,
                totalEpisodesWatched = list.sumOf { it.episodesWatched },
                mangaCount = manga.size,
                lightNovelCount = novels.size,
                averageRating = if (rated.isEmpty()) 0.0 else rated.map { it.rating }.average(),
                ratedCount = rated.size,
                topGenres = genreCounts
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ProfileStats()
            )

        loadHomeFeed()
        loadDiscover()
        loadAiRecommendations()
        loadSchedule()
        scanLinkedFolder()
    }

    fun setTheme(theme: AppThemeOption) {
        themePrefs.setTheme(theme)
        _themeOption.value = theme
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChange(status: AnimeStatus?) {
        _statusFilter.value = status
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _isHomeFeedLoading.value = true
            _homeFeedError.value = null

            val trendingResult = aniListRepository.getTrending()
            val popularResult = aniListRepository.getPopularThisSeason()
            val topRatedResult = aniListRepository.getTopRated()
            val newReleasesResult = aniListRepository.getNewReleases()
            val recommendedResult = aniListRepository.getRecommended()

            trendingResult.onSuccess { _trending.value = it }
            popularResult.onSuccess { _popularThisSeason.value = it }
            topRatedResult.onSuccess { _topRated.value = it }
            newReleasesResult.onSuccess { _newReleases.value = it }
            recommendedResult.onSuccess { _recommended.value = it }

            val allFailed = listOf(trendingResult, popularResult, topRatedResult, newReleasesResult, recommendedResult)
                .all { it.isFailure }
            if (allFailed) {
                _homeFeedError.value = "Couldn't load your home feed. Check your connection and try again."
            }

            _isHomeFeedLoading.value = false
            _isAppReady.value = true
        }
    }

    /**
     * Asks Gemini for recommendations based on the user's local watchlist,
     * then enriches each returned title against AniList so it can render as
     * a normal poster card (art, score, tap-through to details). Titles
     * Gemini suggests that AniList search can't match are silently dropped
     * rather than shown without art.
     */
    fun loadAiRecommendations() {
        viewModelScope.launch {
            // .first() on the raw repository flow (not the stateIn'd
            // allLocalAnime) so this doesn't race Room's first emission on
            // cold start.
            val watched = repository.allAnime.first()
                .filter { it.status != AnimeStatus.PLAN_TO_WATCH }

            if (watched.size < 3) {
                _aiRecommendationsError.value =
                    "Track a few more anime to unlock AI picks based on your taste."
                return@launch
            }

            _isLoadingAiRecommendations.value = true
            _aiRecommendationsError.value = null

            geminiRepository.getRecommendations(watched)
                .onSuccess { recs ->
                    val enriched = recs.mapNotNull { rec ->
                        aniListRepository.searchAnime(rec.title).getOrNull()?.firstOrNull()
                    }
                    _aiRecommendations.value = enriched.map {
                        it.toHomeCardItem(localByAniListId.value[it.id])
                    }
                    if (enriched.isEmpty()) {
                        _aiRecommendationsError.value = "Couldn't match any AI picks right now."
                    }
                }
                .onFailure { e ->
                    _aiRecommendationsError.value = e.message
                        ?: "Couldn't load AI picks. Check your connection and try again."
                }

            _isLoadingAiRecommendations.value = false
        }
    }

    /**
     * Sends a user message to the AI recs chat and appends Gemini's reply.
     * The whole conversation so far is resent each time (Gemini's API is
     * stateless per-call), so this stays coherent across follow-up
     * questions like "what about something shorter?"
     */
    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isChatLoading.value) return

        viewModelScope.launch {
            // Snapshot the history BEFORE inserting the new message: Room's
            // Flow updates asynchronously, so chatMessages.value might not
            // reflect the insert we're about to do until the next emission.
            val historySoFar = chatMessages.value
            val userMessage = ChatMessage(isUser = true, text = text)
            chatRepository.addMessage(userMessage)

            _isChatLoading.value = true
            _chatError.value = null

            val watched = repository.allAnime.first()
                .filter { it.status != AnimeStatus.PLAN_TO_WATCH }
            val conversation = (historySoFar + userMessage).map { it.isUser to it.text }

            geminiChatRepository.sendMessage(conversation, watched)
                .onSuccess { chatReply ->
                    val enrichedRecs = chatReply.recommendations.mapNotNull { rec ->
                        aniListRepository.searchAnime(rec.title).getOrNull()?.firstOrNull()
                    }.map { it.toHomeCardItem(localByAniListId.value[it.id]) }

                    chatRepository.addMessage(
                        ChatMessage(isUser = false, text = chatReply.reply, recommendations = enrichedRecs)
                    )
                }
                .onFailure { e ->
                    _chatError.value = e.message ?: "Couldn't reach the AI. Check your connection and try again."
                }

            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch { chatRepository.clearAll() }
        _chatError.value = null
    }

    fun searchOnline(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _searchApiError.value = null
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _isSearchingApi.value = true
            _searchApiError.value = null
            aniListRepository.searchAnime(query)
                .onSuccess { _searchResults.value = it }
                .onFailure { _searchApiError.value = "Couldn't reach the anime database. Check your connection." }
            _isSearchingApi.value = false
        }
    }

    fun clearSearchResults() {
        searchJob?.cancel()
        _searchResults.value = emptyList()
        _searchApiError.value = null
        _isSearchingApi.value = false
    }

    fun addAnimeFromSearchResult(result: AniListMedia) {
        viewModelScope.launch {
            repository.insert(
                Anime(
                    name = result.displayTitle,
                    totalEpisodes = result.episodes ?: 0,
                    imageUrl = result.posterUrl,
                    aniListId = result.id,
                    episodeDurationMinutes = result.duration,
                    genres = result.genres
                )
            )
        }
    }

    // --- Schedule tab ---

    fun selectScheduleDate(date: LocalDate) {
        _scheduleDate.value = date
        loadSchedule(date)
    }

    fun loadSchedule(date: LocalDate = _scheduleDate.value) {
        viewModelScope.launch {
            _isScheduleLoading.value = true
            _scheduleError.value = null

            val zone = ZoneId.systemDefault()
            val dayStart = date.atStartOfDay(zone).toEpochSecond()
            val dayEnd = date.plusDays(1).atStartOfDay(zone).toEpochSecond()

            aniListRepository.getAiringSchedule(dayStart, dayEnd)
                .onSuccess { _scheduleEntries.value = it }
                .onFailure { e ->
                    _scheduleError.value = e.message ?: "Couldn't load the schedule. Check your connection and try again."
                }

            _isScheduleLoading.value = false
        }
    }

    // --- Light novels tab ---

    fun addLightNovel(title: String, uri: String) {
        viewModelScope.launch { lightNovelRepository.addNovel(title, uri) }
    }

    fun removeLightNovel(novel: LightNovelEntity) {
        viewModelScope.launch { lightNovelRepository.removeNovel(novel) }
    }

    /**
     * Called after the UI has already taken a persistable read permission
     * on [uri] via ContentResolver — this just remembers the folder and
     * scans it.
     */
    fun linkFolder(uri: Uri) {
        lightNovelFolderPrefs.setFolderUri(uri.toString())
        _linkedFolderUri.value = uri.toString()
        scanLinkedFolder()
    }

    fun unlinkFolder() {
        _linkedFolderUri.value?.let { uriString ->
            try {
                getApplication<Application>().contentResolver.releasePersistableUriPermission(
                    Uri.parse(uriString),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Permission was already gone; nothing to clean up.
            }
        }
        lightNovelFolderPrefs.setFolderUri(null)
        _linkedFolderUri.value = null
        _linkedFolderName.value = null
        _folderNovels.value = emptyList()
    }

    /** Re-reads the linked folder's contents. Safe to call any time (e.g. a pull-to-refresh). */
    fun scanLinkedFolder() {
        val uriString = _linkedFolderUri.value ?: return

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val tree = DocumentFile.fromTreeUri(getApplication(), Uri.parse(uriString))
                    val name = tree?.name
                    val pdfs = tree?.listFiles()
                        ?.filter { doc ->
                            doc.isFile &&
                                (doc.type == "application/pdf" || doc.name?.endsWith(".pdf", ignoreCase = true) == true)
                        }
                        ?.map { doc ->
                            FolderPdf(
                                title = doc.name?.removeSuffix(".pdf") ?: "Untitled",
                                uri = doc.uri.toString()
                            )
                        }
                        ?.sortedBy { it.title.lowercase() }
                        ?: emptyList()
                    name to pdfs
                }
            }

            result.onSuccess { (name, pdfs) ->
                _linkedFolderName.value = name
                _folderNovels.value = pdfs
            }.onFailure {
                // Folder likely got moved/deleted/permission revoked outside
                // the app; unlink so the UI doesn't keep showing a dead link.
                unlinkFolder()
            }
        }
    }

    // --- Manga: search ---

    fun onMangaSearchQueryChange(query: String) {
        _mangaSearchQuery.value = query
    }

    fun searchManga(query: String) {
        if (query.isBlank()) {
            _mangaSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isMangaSearchLoading.value = true
            _mangaSearchError.value = null

            mangaDexRepository.searchManga(query)
                .onSuccess { _mangaSearchResults.value = it }
                .onFailure { e ->
                    _mangaSearchError.value = e.message ?: "Couldn't search MangaDex. Check your connection and try again."
                }

            _isMangaSearchLoading.value = false
        }
    }

    fun clearMangaSearch() {
        _mangaSearchQuery.value = ""
        _mangaSearchResults.value = emptyList()
        _mangaSearchError.value = null
    }

    fun addMangaToLibrary(manga: MangaDexManga) {
        viewModelScope.launch {
            mangaRepository.addManga(
                MangaEntity(
                    mangaDexId = manga.id,
                    title = manga.displayTitle,
                    coverUrl = manga.coverUrl
                )
            )
        }
    }

    fun removeMangaFromLibrary(manga: MangaEntity) {
        viewModelScope.launch { mangaRepository.removeManga(manga) }
    }

    // --- Manga: chapters ---

    /**
     * Call this the moment a manga is tapped (search result or library
     * item) — [title] gets stored immediately so the Chapters screen has
     * something to show in its header before the chapter list itself
     * finishes loading.
     */
    fun loadChapters(mangaId: String, title: String) {
        _selectedMangaTitle.value = title
        _mangaChapters.value = emptyList()
        _chaptersError.value = null

        viewModelScope.launch {
            _isChaptersLoading.value = true

            mangaDexRepository.getChapters(mangaId)
                .onSuccess { _mangaChapters.value = it }
                .onFailure { e ->
                    _chaptersError.value = e.message ?: "Couldn't load chapters. Check your connection and try again."
                }

            _isChaptersLoading.value = false
        }
    }

    // --- Manga: reader ---

    fun loadChapterPages(chapterId: String) {
        _chapterPages.value = emptyList()
        _pagesError.value = null

        viewModelScope.launch {
            _isPagesLoading.value = true

            mangaDexRepository.getPageUrls(chapterId)
                .onSuccess { _chapterPages.value = it }
                .onFailure { e ->
                    _pagesError.value = e.message ?: "Couldn't load this chapter. Check your connection and try again."
                }

            _isPagesLoading.value = false
        }
    }

    fun addAnime(
        name: String,
        episodesWatched: Int,
        totalEpisodes: Int,
        status: AnimeStatus,
        rating: Int
    ) {
        viewModelScope.launch {
            repository.insert(
                Anime(
                    name = name,
                    episodesWatched = episodesWatched,
                    totalEpisodes = totalEpisodes,
                    status = status,
                    rating = rating
                )
            )
        }
    }

    fun updateAnime(anime: Anime) {
        viewModelScope.launch {
            repository.update(anime)
        }
    }

    fun incrementEpisode(anime: Anime) {
        viewModelScope.launch {
            val cap = if (anime.totalEpisodes > 0) anime.totalEpisodes else Int.MAX_VALUE
            val next = (anime.episodesWatched + 1).coerceAtMost(cap)
            repository.update(anime.copy(episodesWatched = next))
        }
    }

    fun deleteAnime(anime: Anime) {
        viewModelScope.launch {
            repository.delete(anime)
        }
    }

    // --- Details screen actions ---

    fun loadAnimeDetails(aniListId: Int) {
        viewModelScope.launch {
            _isDetailsLoading.value = true
            _detailsError.value = null
            aniListRepository.getAnimeDetails(aniListId)
                .onSuccess { _animeDetails.value = it }
                .onFailure { _detailsError.value = "Couldn't load details. Check your connection and try again." }
            _isDetailsLoading.value = false
        }
    }

    fun loadAnimeCharacters(aniListId: Int) {
        viewModelScope.launch {
            aniListRepository.getAnimeCharacters(aniListId)
                .onSuccess { _characters.value = it }
            // Characters are a nice-to-have; fail silently so a missing
            // characters list never blocks the rest of the details page.
        }
    }

    fun clearAnimeDetails() {
        _animeDetails.value = null
        _detailsError.value = null
        _characters.value = emptyList()
    }

    /** Sets (or creates, if not yet tracked) the list status for this anime. */
    fun setAnimeStatus(details: AniListMedia, existing: Anime?, status: AnimeStatus) {
        viewModelScope.launch {
            if (existing != null) {
                repository.update(existing.copy(status = status))
            } else {
                repository.insert(
                    Anime(
                        name = details.displayTitle,
                        totalEpisodes = details.episodes ?: 0,
                        status = status,
                        imageUrl = details.posterUrl,
                        aniListId = details.id,
                        episodeDurationMinutes = details.duration,
                        genres = details.genres
                    )
                )
            }
        }
    }

    fun rateAnime(anime: Anime, rating: Int) {
        viewModelScope.launch {
            repository.update(anime.copy(rating = rating))
        }
    }

    fun toggleFavorite(anime: Anime) {
        viewModelScope.launch {
            repository.update(anime.copy(isFavorite = !anime.isFavorite))
        }
    }

    // --- Discover tab actions ---

    fun setDiscoverGenre(genre: String?) {
        _discoverGenre.value = genre
        loadDiscover()
    }

    fun setDiscoverSeason(season: String?) {
        _discoverSeason.value = season
        loadDiscover()
    }

    fun setDiscoverYear(year: Int?) {
        _discoverYear.value = year
        loadDiscover()
    }

    fun loadDiscover() {
        discoverJob?.cancel()
        discoverJob = viewModelScope.launch {
            _isDiscoverLoading.value = true
            _discoverError.value = null
            aniListRepository.discoverAnime(
                genre = _discoverGenre.value,
                season = _discoverSeason.value,
                seasonYear = _discoverYear.value
            ).onSuccess { _discoverResults.value = it }
                .onFailure { _discoverError.value = "Couldn't load results. Check your connection and try again." }
            _isDiscoverLoading.value = false
        }
    }

    // --- Search tab actions ---

    fun onCatalogQueryChange(query: String) {
        _catalogQuery.value = query
        catalogJob?.cancel()
        if (query.isBlank()) {
            _catalogResults.value = emptyList()
            _catalogError.value = null
            _isCatalogSearching.value = false
            return
        }
        catalogJob = viewModelScope.launch {
            delay(400)
            _isCatalogSearching.value = true
            _catalogError.value = null
            aniListRepository.searchAnime(query)
                .onSuccess { _catalogResults.value = it }
                .onFailure { _catalogError.value = "Couldn't reach the anime database. Check your connection." }
            _isCatalogSearching.value = false
        }
    }

    // --- Profile actions ---

    fun setDisplayName(name: String) {
        profilePrefs.setDisplayName(name)
        _profileDisplayName.value = name
    }

    /**
     * Copies the picked photo into the app's private storage and remembers
     * its path, since content:// URIs from the photo picker aren't
     * guaranteed to stay readable after the app process dies.
     */
    fun setProfileBanner(uri: Uri) {
        viewModelScope.launch {
            _isBannerSaving.value = true
            val savedPath = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    val destFile = File(context.filesDir, "profile_banner.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output -> input.copyTo(output) }
                    }
                    destFile.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
            if (savedPath != null) {
                profilePrefs.setBannerPath(savedPath)
                _profileBannerPath.value = savedPath
            }
            _isBannerSaving.value = false
        }
    }

    /** Same idea as [setProfileBanner], but for the circular avatar photo. */
    fun setProfileAvatar(uri: Uri) {
        viewModelScope.launch {
            _isAvatarSaving.value = true
            val savedPath = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    val destFile = File(context.filesDir, "profile_avatar.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output -> input.copyTo(output) }
                    }
                    destFile.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
            if (savedPath != null) {
                profilePrefs.setAvatarPath(savedPath)
                _profileAvatarPath.value = savedPath
            }
            _isAvatarSaving.value = false
        }
    }
}
