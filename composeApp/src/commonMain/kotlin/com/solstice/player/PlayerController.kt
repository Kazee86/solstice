package com.solstice.player

import com.solstice.model.PlaybackState
import com.solstice.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AudioPlayerBackend {
    fun play(song: Song)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun stop()
}

class PlayerController(
    private var backend: AudioPlayerBackend? = null
) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    fun attachBackend(audioBackend: AudioPlayerBackend) {
        this.backend = audioBackend
    }

    fun playSong(song: Song) {
        val currentQueue = _state.value.queue
        val index = currentQueue.indexOfFirst { it.id == song.id }
        val newQueue = if (index != -1) currentQueue else listOf(song) + currentQueue
        val newIndex = if (index != -1) index else 0

        _state.value = _state.value.copy(
            currentSong = song,
            isPlaying = true,
            positionMs = 0L,
            queue = newQueue,
            queueIndex = newIndex
        )
        backend?.play(song)
    }

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val startSong = songs.getOrNull(startIndex) ?: songs[0]
        _state.value = _state.value.copy(
            currentSong = startSong,
            isPlaying = true,
            positionMs = 0L,
            queue = songs,
            queueIndex = startIndex
        )
        backend?.play(startSong)
    }

    fun togglePlayPause() {
        val current = _state.value
        if (current.currentSong == null) return

        if (current.isPlaying) {
            backend?.pause()
            _state.value = current.copy(isPlaying = false)
        } else {
            backend?.resume()
            _state.value = current.copy(isPlaying = true)
        }
    }

    fun seekTo(posMs: Long) {
        backend?.seekTo(posMs)
        _state.value = _state.value.copy(positionMs = posMs)
    }

    fun next() {
        val current = _state.value
        if (current.queue.isEmpty()) return
        val nextIdx = (current.queueIndex + 1) % current.queue.size
        val nextSong = current.queue[nextIdx]
        _state.value = current.copy(
            currentSong = nextSong,
            queueIndex = nextIdx,
            positionMs = 0L
        )
        backend?.play(nextSong)
    }

    fun previous() {
        val current = _state.value
        if (current.queue.isEmpty()) return
        val prevIdx = if (current.queueIndex - 1 < 0) current.queue.size - 1 else current.queueIndex - 1
        val prevSong = current.queue[prevIdx]
        _state.value = current.copy(
            currentSong = prevSong,
            queueIndex = prevIdx,
            positionMs = 0L
        )
        backend?.play(prevSong)
    }

    fun updateProgress(posMs: Long, durMs: Long) {
        _state.value = _state.value.copy(
            positionMs = posMs,
            durationMs = durMs
        )
    }

    companion object {
        val Instance = PlayerController()
    }
}
