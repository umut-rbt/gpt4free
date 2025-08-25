package com.example.personalai.nlu

data class IntentResult(
	val intent: IntentType,
	val parameters: Map<String, String> = emptyMap()
)

enum class IntentType {
	REMEMBER,
	RECALL,
	SET_ALARM,
	GET_LOCATION,
	OPEN_CAMERA,
	SMALL_TALK,
	UNKNOWN
}

class IntentParser {
	fun parse(input: String): IntentResult {
		val text = input.lowercase()
		return when {
			text.startsWith("remember ") || text.contains("hafızaya al") ->
				IntentResult(IntentType.REMEMBER, mapOf("text" to input.removePrefix("remember ").trim()))

			text.startsWith("recall ") || text.contains("hatırla") || text.startsWith("what did i say about") ->
				IntentResult(IntentType.RECALL, mapOf("query" to input.removePrefix("recall ").trim()))

			text.contains("alarm") -> {
				val time = Regex("(\\d{1,2}):(\\d{2})").find(text)?.value ?: ""
				IntentResult(IntentType.SET_ALARM, if (time.isNotEmpty()) mapOf("time" to time) else emptyMap())
			}

			text.contains("konum") || text.contains("where am i") || text.contains("location") ->
				IntentResult(IntentType.GET_LOCATION)

			text.contains("kamera") || text.contains("camera") ->
				IntentResult(IntentType.OPEN_CAMERA)

			text.contains("hello") || text.contains("merhaba") || text.contains("nasılsın") ->
				IntentResult(IntentType.SMALL_TALK)

			else -> IntentResult(IntentType.UNKNOWN)
		}
	}
}