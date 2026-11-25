package org.yusufteker.routealarm.feature.location.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

actual class AlarmSoundPlayer {
    private var player: AVAudioPlayer? = null
    private var autoStopJob: Job? = null
    private var vibrationJob: Job? = null
    private var playing: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Main)

    @OptIn(ExperimentalForeignApi::class)
    actual fun play(withVibration: Boolean) {
        stop()

        runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        }

        val path = NSBundle.mainBundle.pathForResource("alarm_sound", "mp3")
        if (path != null) {
            val url = NSURL.fileURLWithPath(path)
            player = AVAudioPlayer(contentsOfURL = url, error = null).apply {
                prepareToPlay()
                numberOfLoops = -1
                play()
            }
            playing = player != null
        }

        if (withVibration) {
            vibrationJob = scope.launch {
                while (isActive && playing) {
                    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
                    delay(1200L)
                }
            }
        }

        autoStopJob = scope.launch {
            delay(30_000L)
            stop()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stop() {
        autoStopJob?.cancel()
        autoStopJob = null
        vibrationJob?.cancel()
        vibrationJob = null

        player?.stop()
        player = null
        playing = false

        runCatching {
            AVAudioSession.sharedInstance().setActive(false, error = null)
        }
    }

    actual fun isPlaying(): Boolean = playing
}
