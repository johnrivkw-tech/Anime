package com.example.animetracker.data

import kotlinx.coroutines.flow.Flow

class LightNovelRepository(private val dao: LightNovelDao) {
    val allNovels: Flow<List<LightNovelEntity>> = dao.getAll()

    suspend fun addNovel(title: String, uri: String) {
        dao.insert(LightNovelEntity(title = title, uri = uri))
    }

    suspend fun removeNovel(novel: LightNovelEntity) {
        dao.delete(novel)
    }

    suspend fun removeAll() {
        dao.deleteAll()
    }
}
