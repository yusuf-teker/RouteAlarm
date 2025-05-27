package org.yusufteker.routealarm.feature.alarm.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.android.ext.android.inject
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer

class AlarmActivity : ComponentActivity() {


    private val alarmSoundPlayer: AlarmSoundPlayer by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        alarmSoundPlayer.play()

        setContent {
            AlarmScreen(
                onStopAlarm = { // todo düzenlenecek
                    alarmSoundPlayer.stop()
                    stopService(Intent(this, LocationTrackingService::class.java))
                    finish()
                }
            )
        }
    }
}
@Composable
fun AlarmScreen(onStopAlarm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Alarm Çalıyor!",
                fontSize = 24.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onStopAlarm) {
                Text("Alarmı Durdur")
            }
        }
    }
}

