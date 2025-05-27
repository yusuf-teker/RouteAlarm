package org.yusufteker.routealarm.feature.location.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import org.yusufteker.routealarm.R

actual class AlarmSoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var stopHandler: Handler? = null
    private var stopRunnable: Runnable? = null



    actual fun play() {

        stop()

        if (mediaPlayer == null) {
            val afd = context.resources.openRawResourceFd(R.raw.alarm_sound) ?: return

            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            mediaPlayer?.isLooping = true
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }

        mediaPlayer?.start()


        stopHandler = Handler(Looper.getMainLooper())
        stopRunnable = Runnable {
            stop()
        }
        stopHandler?.postDelayed(stopRunnable!!, 30_000L)
    }

    actual fun stop() {

        stopHandler?.removeCallbacks(stopRunnable!!)
        stopHandler = null
        stopRunnable = null

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

    }
}
