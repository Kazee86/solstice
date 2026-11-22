package com.solstice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.solstice.model.Song
import com.solstice.player.DesktopAudioPlayer
import com.solstice.player.PlayerController
import com.solstice.scanner.DesktopMediaScanner
import java.io.File
import javax.swing.JFileChooser

fun main() = application {
    val desktopPlayer = remember { DesktopAudioPlayer() }
    remember { PlayerController.Instance.attachBackend(desktopPlayer) }

    var localSongs by remember { mutableStateOf<List<Song>>(emptyList()) }

    // Auto-scan user default Music folder on startup
    remember {
        val defaultMusicDir = File(System.getProperty("user.home"), "Music")
        if (defaultMusicDir.exists()) {
            localSongs = DesktopMediaScanner.scanFolder(defaultMusicDir)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Solstice - Hybrid Music Player",
        state = WindowState(width = 960.dp, height = 800.dp)
    ) {
        App(
            localSongs = localSongs,
            onScanLocalRequested = {
                val chooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    dialogTitle = "Pilih Folder Koleksi Musik"
                }
                val res = chooser.showOpenDialog(null)
                if (res == JFileChooser.APPROVE_OPTION) {
                    val dir = chooser.selectedFile
                    localSongs = DesktopMediaScanner.scanFolder(dir)
                }
            }
        )
    }
}
