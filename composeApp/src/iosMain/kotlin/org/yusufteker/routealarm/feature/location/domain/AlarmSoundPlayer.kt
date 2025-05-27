package org.yusufteker.routealarm.feature.location.domain

actual class AlarmSoundPlayer {
    actual fun play() {
        // iOS özel ses çalma (AVFoundation vb.)
    }

    actual fun stop() {
        // iOS özel durdurma
    }
}
