package org.yusufteker.routealarm.feature.location.domain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle


actual class AlarmSoundPlayer {
    private var player: AVAudioPlayer? = null



    @OptIn(ExperimentalForeignApi::class)
    actual fun play() {
        val path = NSBundle.mainBundle.pathForResource("alarm_sound", "mp3") ?: return
        val url = NSURL.fileURLWithPath(path)

        player = AVAudioPlayer(contentsOfURL = url, error = null).apply {
            prepareToPlay()
            numberOfLoops = -1 // sonsuz döngü

            play()
        }
    }

    actual fun stop() {
        player?.stop()
        player = null
    }
}
