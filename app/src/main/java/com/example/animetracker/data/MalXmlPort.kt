package com.example.animetracker.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.InputStream
import java.io.StringWriter

/**
 * Reads and writes the classic MyAnimeList XML export format (the file you
 * get from MAL's Profile > List > Export, and the format most other
 * trackers speak too).
 *
 * Only the fields this app actually tracks (title, episodes watched, total
 * episodes, score, status) are round-tripped. Everything else is left out
 * on export and ignored on import, since this app has no place to put it.
 */
object MalXmlPort {

    /** One `<anime>` row from a parsed MAL file, before it's matched against the local library. */
    data class MalEntry(
        val title: String,
        val episodesWatched: Int,
        val totalEpisodes: Int,
        val score: Int,
        val status: AnimeStatus
    )

    /** Builds a MAL-compatible XML export string for the given library. */
    fun export(animeList: List<Anime>): String {
        val writer = StringWriter()
        val serializer: XmlSerializer = Xml.newSerializer()
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        serializer.text("\n")
        serializer.startTag(null, "myanimelist")

        serializer.text("\n")
        serializer.startTag(null, "myinfo")
        tag(serializer, "user_export_type", "1")
        tag(serializer, "user_total_anime", animeList.size.toString())
        serializer.endTag(null, "myinfo")

        animeList.forEach { anime ->
            serializer.text("\n")
            serializer.startTag(null, "anime")
            tag(serializer, "series_animedb_id", (anime.aniListId ?: 0).toString())
            tag(serializer, "series_title", anime.name)
            tag(serializer, "series_episodes", anime.totalEpisodes.toString())
            tag(serializer, "my_watched_episodes", anime.episodesWatched.toString())
            tag(serializer, "my_score", anime.rating.toString())
            tag(serializer, "my_status", anime.status.toMalStatus())
            tag(serializer, "update_on_import", "1")
            serializer.endTag(null, "anime")
        }

        serializer.text("\n")
        serializer.endTag(null, "myanimelist")
        serializer.endDocument()
        return writer.toString()
    }

    private fun tag(serializer: XmlSerializer, name: String, value: String) {
        serializer.startTag(null, name)
        serializer.text(value)
        serializer.endTag(null, name)
    }

    /** Parses a MAL XML export (or compatible file) into a flat list of entries. */
    fun parse(input: InputStream): List<MalEntry> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)

        val entries = mutableListOf<MalEntry>()
        var inAnime = false
        var title = ""
        var watched = 0
        var total = 0
        var score = 0
        var status = "Plan to Watch"

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "anime" -> {
                        inAnime = true
                        title = ""
                        watched = 0
                        total = 0
                        score = 0
                        status = "Plan to Watch"
                    }
                    "series_title" -> if (inAnime) title = parser.nextText().trim()
                    "series_episodes" -> if (inAnime) total = parser.nextText().trim().toIntOrNull() ?: 0
                    "my_watched_episodes" -> if (inAnime) watched = parser.nextText().trim().toIntOrNull() ?: 0
                    "my_score" -> if (inAnime) score = parser.nextText().trim().toIntOrNull() ?: 0
                    "my_status" -> if (inAnime) status = parser.nextText().trim()
                }
                XmlPullParser.END_TAG -> if (parser.name == "anime" && inAnime) {
                    if (title.isNotBlank()) {
                        entries += MalEntry(
                            title = title,
                            episodesWatched = watched,
                            totalEpisodes = total,
                            score = score.coerceIn(0, 10),
                            status = status.toAppStatus()
                        )
                    }
                    inAnime = false
                }
            }
            eventType = parser.next()
        }
        return entries
    }

    private fun AnimeStatus.toMalStatus(): String = when (this) {
        AnimeStatus.WATCHING -> "Watching"
        AnimeStatus.COMPLETED -> "Completed"
        AnimeStatus.PLAN_TO_WATCH -> "Plan to Watch"
    }

    /**
     * MAL has five statuses; this app only tracks three. On-Hold comes in
     * as Watching (it's still in progress, just paused) and Dropped comes
     * in as Plan to Watch (not currently being watched, and there's no
     * separate "dropped" bucket here to put it in).
     */
    private fun String.toAppStatus(): AnimeStatus = when (this) {
        "Watching" -> AnimeStatus.WATCHING
        "Completed" -> AnimeStatus.COMPLETED
        "On-Hold" -> AnimeStatus.WATCHING
        "Dropped" -> AnimeStatus.PLAN_TO_WATCH
        "Plan to Watch" -> AnimeStatus.PLAN_TO_WATCH
        else -> AnimeStatus.PLAN_TO_WATCH
    }
}
