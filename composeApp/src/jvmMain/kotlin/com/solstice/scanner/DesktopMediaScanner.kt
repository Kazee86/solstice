package com.solstice.scanner

import com.solstice.model.AudioSource
import com.solstice.model.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

object DesktopMediaScanner {
    private val SUPPORTED_EXTENSIONS = setOf("mp3", "flac", "m4a", "wav", "ogg")

    fun scanFolder(folder: File): List<Song> {
        if (!folder.exists() || !folder.isDirectory) return emptyList()

        val songs = mutableListOf<Song>()
        try {
            Files.walk(folder.toPath())
                .filter { Files.isRegularFile(it) }
                .filter { path ->
                    val ext = path.fileName.toString().substringAfterLast('.', "").lowercase()
                    ext in SUPPORTED_EXTENSIONS
                }
                .forEach { path ->
                    val song = parseFile(path)
                    if (song != null) {
                        songs.add(song)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs.sortedBy { it.title }
    }

    private fun parseFile(path: Path): Song? {
        val file = path.toFile()
        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            val header = audioFile.audioHeader

            val title = tag?.getFirst(FieldKey.TITLE)?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
            val artist = tag?.getFirst(FieldKey.ARTIST)?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val album = tag?.getFirst(FieldKey.ALBUM) ?: ""
            val durationMs = (header?.trackLength?.toLong() ?: 0L) * 1000L

            Song(
                id = "desktop_${UUID.nameUUIDFromBytes(file.absolutePath.toByteArray())}",
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                source = AudioSource.Local(pathOrUri = file.absolutePath)
            )
        } catch (e: Exception) {
            Song(
                id = "desktop_${UUID.nameUUIDFromBytes(file.absolutePath.toByteArray())}",
                title = file.nameWithoutExtension,
                artist = "Unknown Artist",
                durationMs = 0L,
                source = AudioSource.Local(pathOrUri = file.absolutePath)
            )
        }
    }
}
