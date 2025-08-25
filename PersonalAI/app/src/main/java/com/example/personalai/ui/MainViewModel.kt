package com.example.personalai.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalai.actions.DeviceActions
import com.example.personalai.nlu.IntentParser
import com.example.personalai.nlu.IntentType
import com.example.personalai.rag.RagService
import com.example.personalai.speech.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
	private val rag = RagService(app.applicationContext)
	private val parser = IntentParser()
	private val tts = TtsManager(app.applicationContext)
	private val actions = DeviceActions(app.applicationContext)

	private val _ui = MutableStateFlow(UiState())
	val ui: StateFlow<UiState> = _ui

	fun onUserSubmit(text: String) {
		viewModelScope.launch {
			process(text)
		}
	}

	private suspend fun process(text: String) {
		_ui.value = _ui.value.copy(isProcessing = true)
		val parsed = parser.parse(text)
		when (parsed.intent) {
			IntentType.REMEMBER -> {
				val rememberedText = parsed.parameters["text"] ?: text
				rag.addMemory(rememberedText, source = "user", importance = 1)
				respond("Kaydettim: $rememberedText")
			}
			IntentType.RECALL -> {
				val query = parsed.parameters["query"] ?: text
				val res = rag.retrieveRelevant(query)
				val msg = if (res.isEmpty()) "Buna dair bir şey hatırlamıyorum." else res.joinToString("\n") { (m, s) -> "${"%.2f".format(s)}: ${m.text}" }
				respond(msg)
			}
			IntentType.SET_ALARM -> {
				val time = parsed.parameters["time"] ?: ""
				val parts = time.split(":")
				if (parts.size == 2) {
					val hour = parts[0].toIntOrNull() ?: 9
					val minute = parts[1].toIntOrNull() ?: 0
					val i: Intent = actions.setAlarmIntent(hour, minute)
					_ui.value = _ui.value.copy(pendingIntent = i)
					respond("Alarm ${"%02d".format(hour)}:${"%02d".format(minute)} için ayarlandı.")
				} else {
					respond("Lütfen zamanı HH:MM formatında söyleyin.")
				}
			}
			IntentType.OPEN_CAMERA -> {
				val i = actions.openCameraIntent()
				_ui.value = _ui.value.copy(pendingIntent = i)
				respond("Kamerayı açıyorum.")
			}
			IntentType.GET_LOCATION -> {
				respond("Konum izni gerekli. İzin verirseniz paylaşabilirim.")
			}
			IntentType.SMALL_TALK -> {
				respond("Merhaba! Nasıl yardımcı olabilirim?")
			}
			IntentType.UNKNOWN -> {
				respond("Bunu nasıl yapacağımı öğrenmedim ama not alabilirim.")
			}
		}
		_ui.value = _ui.value.copy(isProcessing = false)
	}

	private fun respond(text: String) {
		tts.speak(text)
		_ui.value = _ui.value.copy(response = text)
	}
}

data class UiState(
	val isProcessing: Boolean = false,
	val response: String = "",
	val pendingIntent: Intent? = null
)