package com.solstice.network

import com.solstice.model.LyricLine
import com.solstice.model.Lyrics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class LrclibResponse(
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)

class LyricsClient(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) {
    suspend fun getLyrics(title: String, artist: String, durationSec: Long = 0): Lyrics {
        return try {
            val response: LrclibResponse = client.get("https://lrclib.net/api/get") {
                parameter("track_name", title)
                parameter("artist_name", artist)
                if (durationSec > 0) {
                    parameter("duration", durationSec)
                }
            }.body()

            val lines = response.syncedLyrics?.let { parseLrc(it) } ?: emptyList()
            Lyrics(plainLyrics = response.plainLyrics, syncedLyrics = lines)
        } catch (e: Exception) {
            Lyrics()
        }
    }

    companion object {
        fun parseLrc(lrcContent: String): List<LyricLine> {
            val regex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\](.*)""")
            val result = mutableListOf<LyricLine>()

            lrcContent.lineSequence().forEach { line ->
                val match = regex.find(line.trim())
                if (match != null) {
                    val (minStr, secStr, fracStr, text) = match.destructured
                    val min = minStr.toLongOrNull() ?: 0L
                    val sec = secStr.toLongOrNull() ?: 0L
                    val frac = fracStr.let {
                        if (it.length == 2) it.toLongOrNull()?.times(10) ?: 0L
                        else it.toLongOrNull() ?: 0L
                    }
                    val timeMs = (min * 60 + sec) * 1000 + frac
                    result.add(LyricLine(timeMs, text.trim()))
                }
            }
            return result.sortedBy { it.timeMs }
        }
    }
}
