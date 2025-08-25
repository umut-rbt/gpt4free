package com.example.personalai

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalai.speech.SpeechInputManager
import com.example.personalai.ui.MainViewModel
import com.example.personalai.ui.UiState

class MainActivity : ComponentActivity() {
	private val requestAudio = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
	private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			MaterialTheme {
				Surface(modifier = Modifier.fillMaxSize()) {
					PersonalAiScreen(onRequestAudio = {
						requestAudio.launch(Manifest.permission.RECORD_AUDIO)
					}, onRequestLocation = {
						requestLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
					})
				}
			}
		}
	}
}

@Composable
fun PersonalAiScreen(onRequestAudio: () -> Unit, onRequestLocation: () -> Unit, vm: MainViewModel = viewModel()) {
	val ui by vm.ui.collectAsState()
	val context = LocalContext.current
	var input by remember { mutableStateOf(TextFieldValue("")) }
	var listening by remember { mutableStateOf(false) }
	val speech = remember { SpeechInputManager(context) }

	LaunchedEffect(ui.pendingIntent) {
		ui.pendingIntent?.let { intent ->
			context.startActivity(intent)
		}
	}

	Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			TextField(
				value = input,
				onValueChange = { input = it },
				modifier = Modifier.weight(1f),
				placeholder = { Text("Komut yazın ya da konuşun") }
			)
			Button(onClick = { vm.onUserSubmit(input.text) }) { Text(stringResource(id = R.string.send)) }
		}

		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			Button(onClick = {
				onRequestAudio()
				if (!listening) {
					listening = true
					speech.startListening()
				} else {
					listening = false
					speech.stop()
				}
			}) { Text(if (listening) stringResource(id = R.string.listening) else stringResource(id = R.string.mic)) }

			Button(onClick = onRequestLocation) { Text("Konum izni iste") }
		}

		if (ui.isProcessing) {
			LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
		}

		Text(text = ui.response)
	}

	LaunchedEffect(listening) {
		if (listening) {
			speech.results().collect { partial ->
				input = TextFieldValue(partial)
			}
		}
	}
}