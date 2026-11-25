package org.yusufteker.routealarm.feature.location.domain

expect class AlarmSoundPlayer {
    fun play(withVibration: Boolean = true)
    fun stop()
    fun isPlaying(): Boolean
}
