package org.yusufteker.routealarm.feature.alarm.presentation

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.android.ext.android.inject
import org.yusufteker.routealarm.R
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService.Companion.ACTION_STOP_ACHIEVED
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import kotlin.getValue

class AlarmActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ACTION = "action"

        const val EXTRA_STOP_NAME = "stop_name"
    }


    private val alarmSoundPlayer: AlarmSoundPlayer by inject()
    private val popupManager: PopupManager by inject()


    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1
        val intentAction = intent?.getStringExtra(EXTRA_ACTION) ?: ""
        val stopName = intent?.getStringExtra(EXTRA_STOP_NAME)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        alarmSoundPlayer.play()

        setContent {
            AlarmScreen(
                title = getString(R.string.stop_reached, stopName),
                buttonText = getString(R.string.stop_alarm_text),
                onStopAlarm = {
                    alarmSoundPlayer.stop()
                    popupManager.dismissAll()
                    startService(
                        Intent(this, LocationTrackingService::class.java).apply {
                            action = intentAction
                            putExtra(EXTRA_ALARM_ID, alarmId)

                        })
                    finish()
                })
        }
    }
}

@Composable
fun AlarmScreen(
    title: String,
    buttonText: String,
    onStopAlarm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title, fontSize = 24.sp, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 36.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onStopAlarm) {
                Text(text = buttonText)
            }
        }
    }
}

