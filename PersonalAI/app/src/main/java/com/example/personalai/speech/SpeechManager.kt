package com.example.personalai.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale

class SpeechInputManager(private val context: Context) : RecognitionListener {
	private val recognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
	private val resultsChannel = Channel<String>(Channel.BUFFERED)

	init {
		recognizer.setRecognitionListener(this)
	}

	fun startListening(language: Locale = Locale.getDefault()) {
		val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
			putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
			putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
			putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
		}
		recognizer.startListening(intent)
	}

	fun stop() {
		recognizer.stopListening()
	}

	fun destroy() {
		recognizer.destroy()
	}

	fun results(): Flow<String> = resultsChannel.receiveAsFlow()

	override fun onReadyForSpeech(params: Bundle?) {}
	override fun onBeginningOfSpeech() {}
	override fun onRmsChanged(rmsdB: Float) {}
	override fun onBufferReceived(buffer: ByteArray?) {}
	override fun onEndOfSpeech() {}
	override fun onError(error: Int) {}
	override fun onPartialResults(partialResults: Bundle?) {
		val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
		if (!list.isNullOrEmpty()) {
			resultsChannel.trySend(list.first())
		}
	}
	override fun onResults(results: Bundle?) {
		val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
		if (!list.isNullOrEmpty()) {
			resultsChannel.trySend(list.first())
		}
	}
	override fun onEvent(eventType: Int, params: Bundle?) {}
}

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
	private var tts: TextToSpeech? = TextToSpeech(context, this)
	private var ready: Boolean = false

	override fun onInit(status: Int) {
		ready = status == TextToSpeech.SUCCESS
	}

	fun speak(text: String, language: Locale = Locale.getDefault()) {
		if (ready) {
			tts?.language = language
			tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, System.currentTimeMillis().toString())
		}
	}

	fun shutdown() {
		tts?.shutdown()
		tts = null
	}
}