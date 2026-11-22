package com.solstice.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.solstice.model.AudioSource
import com.solstice.model.Song
import com.solstice.player.AudioPlayerBackend
import com.solstice.player.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService(), AudioPlayerBackend {
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val exo = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()

        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    PlayerController.Instance.next()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) startProgressTracker() else stopProgressTracker()
            }
        })

        player = exo
        mediaSession = MediaSession.Builder(this, exo).build()
        PlayerController.Instance.attachBackend(this)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                player?.let {
                    PlayerController.Instance.updateProgress(it.currentPosition, it.duration.coerceAtLeast(0L))
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun play(song: Song) {
        val uri = when (val src = song.source) {
            is AudioSource.Local -> src.pathOrUri
            is AudioSource.Remote -> src.streamUrl ?: ""
        }
        if (uri.isNotBlank()) {
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.prepare()
            player?.play()
        }
    }

    override fun pause() {
        player?.pause()
    }

    override fun resume() {
        player?.play()
    }

    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    override fun stop() {
        player?.stop()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
