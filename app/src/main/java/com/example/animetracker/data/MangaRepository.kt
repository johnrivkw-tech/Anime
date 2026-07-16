package com.example.animetracker.data

import kotlinx.coroutines.flow.Flow

class MangaRepository(private val dao: MangaDao) {
    val allManga: Flow<List<MangaEntity>> = dao.getAll()

    suspend fun addManga(manga: MangaEntity) = dao.insert(manga)
    suspend fun removeManga(manga: MangaEntity) = dao.delete(manga)
    suspend fun removeAll() = dao.deleteAll()
}
