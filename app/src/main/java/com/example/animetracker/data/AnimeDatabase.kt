    package com.example.animetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Anime::class, ChatMessageEntity::class, LightNovelEntity::class, MangaEntity::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun chatDao(): ChatDao
    abstract fun lightNovelDao(): LightNovelDao
    abstract fun mangaDao(): MangaDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeDatabase? = null

        fun getDatabase(context: Context): AnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "anime_database"
                )
                    // No real Migration is defined yet, so bumping the version
                    // (this one renamed the malId column to aniListId for the
                    // Jikan → AniList switch) just rebuilds the table instead
                    // of crashing. That's fine while you're still developing.
                    // Once you have real data you don't want to lose, replace
                    // this with a proper Room Migration.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
