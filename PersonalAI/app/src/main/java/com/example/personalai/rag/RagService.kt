package com.example.personalai.rag

import android.content.Context
import androidx.room.Room
import com.example.personalai.data.EmbeddingEntity
import com.example.personalai.data.MemoryDatabase
import com.example.personalai.data.MemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RagService(context: Context) {
	private val db = Room.databaseBuilder(context, MemoryDatabase::class.java, MemoryDatabase.NAME).build()
	private val dao = db.memoryDao()
	private val vectorizer = HashingVectorizer()

	suspend fun addMemory(text: String, source: String, importance: Int = 1): Long = withContext(Dispatchers.IO) {
		val memoryId = dao.insertMemory(
			MemoryEntity(
				timestamp = System.currentTimeMillis(),
				text = text,
				source = source,
				importance = importance
			)
		)
		val vector = vectorizer.embed(text)
		dao.insertEmbedding(EmbeddingEntity(memoryId = memoryId, vector = vector))
		memoryId
	}

	suspend fun retrieveRelevant(query: String, topK: Int = 5): List<Pair<MemoryEntity, Float>> = withContext(Dispatchers.IO) {
		val allEmb = dao.getAllEmbeddings()
		val qv = vectorizer.embed(query)
		val scored = allEmb.map { emb ->
			Similarity.cosine(qv, emb.vector) to emb.memoryId
		}.sortedByDescending { it.first }.take(topK)
		val ids = scored.map { it.second }
		val mems = dao.getMemoriesByIds(ids)
		val memById = mems.associateBy { it.id }
		scored.mapNotNull { (score, id) ->
			memById[id]?.let { it to score }
		}
	}
}