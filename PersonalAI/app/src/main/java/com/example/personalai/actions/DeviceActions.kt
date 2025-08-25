package com.example.personalai.actions

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class DeviceActions(private val context: Context) {
	private val fused: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

	fun openCameraIntent(): Intent {
		return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
	}

	fun setAlarmIntent(hour: Int, minute: Int): Intent {
		return Intent(AlarmClock.ACTION_SET_ALARM).apply {
			putExtra(AlarmClock.EXTRA_HOUR, hour)
			putExtra(AlarmClock.EXTRA_MINUTES, minute)
			putExtra(AlarmClock.EXTRA_MESSAGE, "PersonalAI Alarm")
		}
	}

	@RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
	suspend fun getLastKnownLocation(): Location? = fused.lastLocation.await()
}