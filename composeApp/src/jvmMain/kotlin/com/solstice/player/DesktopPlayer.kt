package com.solstice.player

import com.solstice.model.AudioSource
import com.solstice.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class DesktopAudioPlayer : AudioPlayerBackend {
    private var currentClip: Clip? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var progressJob: Job? = null

    override fun play(song: Song) {
        stop()
        try {
            when (val src = song.source) {
                is AudioSource.Local -> {
                    val file = File(src.pathOrUri)
                    if (file.exists() && file.extension.lowercase() == "wav") {
                        val audioIn = AudioSystem.getAudioInputStream(file)
                        currentClip = AudioSystem.getClip().apply {
                            open(audioIn)
                            start()
                        }
                    }
                }
                is AudioSource.Remote -> {
                    // For remote streams or non-wav formats, placeholder / delegate to system player
                }
            }
            startProgress()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                currentClip?.let {
                    val pos = it.microsecondPosition / 1000
                    val dur = it.microsecondLength / 1000
                    PlayerController.Instance.updateProgress(pos, dur)
                }
                delay(500)
            }
        }
    }

    override fun pause() {
        currentClip?.stop()
    }

    override fun resume() {
        currentClip?.start()
    }

    override fun seekTo(positionMs: Long) {
        currentClip?.microsecondPosition = positionMs * 1000
    }

    override fun stop() {
        progressJob?.cancel()
        currentClip?.stop()
        currentClip?.close()
        currentClip = null
    }
}
