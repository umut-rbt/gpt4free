package com.example.personalai.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "memories")
data class MemoryEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val timestamp: Long,
	val text: String,
	val source: String,
	val importance: Int
)

@Entity(
	tableName = "embeddings",
	foreignKeys = [
		ForeignKey(
			entity = MemoryEntity::class,
			parentColumns = ["id"],
			childColumns = ["memoryId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [Index("memoryId")]
)
data class EmbeddingEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val memoryId: Long,
	val vector: FloatArray
)

class Converters {
	@TypeConverter
	fun fromFloatArray(value: FloatArray?): String? = value?.joinToString(",") { it.toString() }

	@TypeConverter
	fun toFloatArray(value: String?): FloatArray? = value?.split(",")?.mapNotNull { it.toFloatOrNull() }?.toFloatArray()
}

@Dao
interface MemoryDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertMemory(memory: MemoryEntity): Long

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertEmbedding(embedding: EmbeddingEntity): Long

	@Transaction
	@Query("SELECT * FROM memories ORDER BY timestamp DESC")
	fun observeMemories(): Flow<List<MemoryEntity>>

	@Query("SELECT * FROM embeddings WHERE memoryId = :memoryId")
	suspend fun getEmbeddingForMemory(memoryId: Long): EmbeddingEntity?

	@Query("SELECT * FROM embeddings")
	suspend fun getAllEmbeddings(): List<EmbeddingEntity>

	@Query("SELECT * FROM memories WHERE id IN (:ids)")
	suspend fun getMemoriesByIds(ids: List<Long>): List<MemoryEntity>
}

@Database(
	entities = [MemoryEntity::class, EmbeddingEntity::class],
	version = 1,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MemoryDatabase : RoomDatabase() {
	abstract fun memoryDao(): MemoryDao

	companion object {
		const val NAME = "personal_ai_memories.db"
	}
}