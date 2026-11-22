package com.solstice.network

import com.solstice.model.AudioSource
import com.solstice.model.Song
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class InnerTubeClient(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) {
    suspend fun search(query: String): List<Song> {
        val payload = buildJsonObject {
            put("context", buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName", "WEB_REMIX")
                    put("clientVersion", "1.20241014.01.00")
                    put("hl", "en")
                    put("gl", "US")
                })
            })
            put("query", query)
            put("params", "Eg-KAQwIABAAGAAgACgAMABqChAEEAMQCRAFEAo%3D") // Music filter
        }

        return try {
            val response: JsonObject = client.post("https://music.youtube.com/youtubei/v1/search") {
                contentType(ContentType.Application.Json)
                header("Origin", "https://music.youtube.com")
                header("Referer", "https://music.youtube.com")
                setBody(payload)
            }.body()

            parseSearchResponse(response)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseSearchResponse(json: JsonObject): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val contents = json["contents"]?.jsonObject
                ?.get("tabbedSearchResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray?.get(0)?.jsonObject
                ?.get("tabRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject
                ?.get("contents")?.jsonArray ?: return emptyList()

            for (section in contents) {
                val shelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
                val items = shelf?.get("contents")?.jsonArray ?: continue

                for (item in items) {
                    val flexItem = item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: continue
                    val flexColumns = flexItem["flexColumns"]?.jsonArray ?: continue
                    if (flexColumns.isEmpty()) continue

                    val titleCol = flexColumns[0].jsonObject["musicResponsiveListItemFlexColumnRenderer"]?.jsonObject
                    val title = titleCol?.get("text")?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject
                        ?.get("text")?.jsonPrimitive?.content ?: "Unknown"

                    var artist = "Unknown Artist"
                    if (flexColumns.size > 1) {
                        val subtitleCol = flexColumns[1].jsonObject["musicResponsiveListItemFlexColumnRenderer"]?.jsonObject
                        val runs = subtitleCol?.get("text")?.jsonObject?.get("runs")?.jsonArray
                        if (!runs.isNullOrEmpty()) {
                            artist = runs[0].jsonObject["text"]?.jsonPrimitive?.content ?: "Unknown Artist"
                        }
                    }

                    val navEndpoint = flexItem["overlay"]?.jsonObject
                        ?.get("musicItemThumbnailOverlayRenderer")?.jsonObject
                        ?.get("content")?.jsonObject
                        ?.get("musicPlayButtonRenderer")?.jsonObject
                        ?.get("playNavigationEndpoint")?.jsonObject

                    val videoId = navEndpoint?.get("watchEndpoint")?.jsonObject
                        ?.get("videoId")?.jsonPrimitive?.content

                    val thumbnails = flexItem["thumbnail"]?.jsonObject
                        ?.get("musicThumbnailRenderer")?.jsonObject
                        ?.get("thumbnail")?.jsonObject
                        ?.get("thumbnails")?.jsonArray

                    val coverUrl = thumbnails?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.content

                    if (!videoId.isNullOrBlank()) {
                        songs.add(
                            Song(
                                id = "yt_$videoId",
                                title = title,
                                artist = artist,
                                coverArtUrl = coverUrl,
                                source = AudioSource.Remote(videoId = videoId)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors on unusual schemas
        }
        return songs
    }
}
