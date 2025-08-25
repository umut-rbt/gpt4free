package com.example.personalai.rag

import kotlin.math.sqrt

class HashingVectorizer(private val vectorSize: Int = 256) {
	fun embed(text: String): FloatArray {
		val vector = FloatArray(vectorSize)
		text.lowercase().split(" ", "\n", "\t", ",", ".", "!", "?").filter { it.isNotBlank() }.forEach { token ->
			val idx = (token.hashCode().toUInt().toLong() % vectorSize).toInt()
			vector[idx] += 1f
		}
		val norm = sqrt(vector.sumOf { (it * it).toDouble() }.toFloat())
		if (norm > 0f) {
			for (i in vector.indices) vector[i] /= norm
		}
		return vector
	}
}

object Similarity {
	fun cosine(a: FloatArray, b: FloatArray): Float {
		val size = minOf(a.size, b.size)
		var dot = 0f
		var na = 0f
		var nb = 0f
		for (i in 0 until size) {
			dot += a[i] * b[i]
			na += a[i] * a[i]
			nb += b[i] * b[i]
		}
		val denom = (kotlin.math.sqrt(na.toDouble()) * kotlin.math.sqrt(nb.toDouble())).toFloat()
		return if (denom > 0f) dot / denom else 0f
	}
}