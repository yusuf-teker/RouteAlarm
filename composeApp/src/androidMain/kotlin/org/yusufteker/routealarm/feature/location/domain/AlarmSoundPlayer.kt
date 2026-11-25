package org.yusufteker.routealarm.feature.location.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import org.yusufteker.routealarm.R

actual class AlarmSoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var stopHandler: Handler? = null
    private var stopRunnable: Runnable? = null
    private var vibrator: Vibrator? = null
    private var playing: Boolean = false

    actual fun play(withVibration: Boolean) {
        stop()

        val afd = context.resources.openRawResourceFd(R.raw.alarm_sound)
        if (afd != null) {
            try {
                val player = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
                mediaPlayer = player
                playing = true
            } catch (e: Exception) {
                mediaPlayer?.release()
                mediaPlayer = null
                playing = false
            } finally {
                runCatching { afd.close() }
            }
        }

        if (withVibration) {
            startVibration()
        }

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable { stop() }
        stopHandler = handler
        stopRunnable = runnable
        handler.postDelayed(runnable, 30_000L)
    }

    private fun startVibration() {
        runCatching {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator = vib
            val pattern = longArrayOf(0, 600, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vib?.vibrate(pattern, 0)
            }
        }
    }

    actual fun stop() {
        stopRunnable?.let { runnable ->
            stopHandler?.removeCallbacks(runnable)
        }
        stopHandler = null
        stopRunnable = null

        runCatching {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        playing = false

        runCatching { vibrator?.cancel() }
        vibrator = null
    }

    actual fun isPlaying(): Boolean = playing
}
