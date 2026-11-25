package org.yusufteker.routealarm.feature.alarm.presentation

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.yusufteker.routealarm.R
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer

class AlarmActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ACTION = "action"
        const val EXTRA_STOP_NAME = "stop_name"
    }

    private val alarmSoundPlayer: AlarmSoundPlayer by inject()
    private val popupManager: PopupManager by inject()
    private val alarmRepository: AlarmRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1
        val intentAction = intent?.getStringExtra(EXTRA_ACTION) ?: LocationTrackingService.ACTION_STOP_SERVICE
        val stopName = intent?.getStringExtra(EXTRA_STOP_NAME).orEmpty()

        if (!alarmSoundPlayer.isPlaying()) {
            alarmSoundPlayer.play(withVibration = true)
        }

        setContent {
            AlarmScreen(
                title = getString(R.string.stop_reached, stopName),
                buttonText = getString(R.string.stop_alarm_text),
                onStopAlarm = {
                    handleStopAlarm(alarmId, intentAction)
                }
            )
        }
    }

    private fun handleStopAlarm(alarmId: Int, intentAction: String) {
        alarmSoundPlayer.stop()
        popupManager.dismissAll()

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (intentAction == LocationTrackingService.ACTION_STOP_SERVICE) {
            notifManager?.cancelAll()
            if (alarmId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    alarmRepository.setStopsPassedForAlarm(alarmId, false)
                    alarmRepository.setAlarmActive(alarmId, false)
                }
            }
            runCatching {
                stopService(Intent(this, LocationTrackingService::class.java))
            }
        } else {
            notifManager?.cancel(org.yusufteker.routealarm.notification.NotificationManager.STOP_ACHIEVED_NOTIFICATION_ID)
            runCatching {
                startService(
                    Intent(this, LocationTrackingService::class.java).apply {
                        action = intentAction
                        putExtra(EXTRA_ALARM_ID, alarmId)
                    }
                )
            }
        }
        finish()
    }
}

@Composable
fun AlarmScreen(
    title: String,
    buttonText: String,
    onStopAlarm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarm_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.background, AppColors.surfaceElevated)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AppColors.accent.copy(alpha = 0.18f))
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(AppColors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AppColors.onAccent,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStopAlarm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.danger,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
