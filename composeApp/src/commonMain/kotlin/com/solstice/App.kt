package com.solstice

import androidx.compose.runtime.Composable
import com.solstice.model.Song
import com.solstice.ui.HomeScreen
import com.solstice.ui.SolsticeTheme

@Composable
fun App(
    localSongs: List<Song> = emptyList(),
    onScanLocalRequested: () -> Unit = {}
) {
    SolsticeTheme {
        HomeScreen(
            localSongs = localSongs,
            onScanLocalRequested = onScanLocalRequested
        )
    }
}
