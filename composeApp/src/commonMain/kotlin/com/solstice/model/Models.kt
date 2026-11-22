package com.solstice.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface AudioSource {
    @Serializable
    data class Remote(val videoId: String, val streamUrl: String? = null) : AudioSource

    @Serializable
    data class Local(val pathOrUri: String) : AudioSource
}

@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val coverArtUrl: String? = null,
    val source: AudioSource
)

@Serializable
data class LyricLine(
    val timeMs: Long,
    val text: String
)

@Serializable
data class Lyrics(
    val plainLyrics: String? = null,
    val syncedLyrics: List<LyricLine> = emptyList()
)

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = 0
)
