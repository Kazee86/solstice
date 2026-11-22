package com.solstice.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solstice.model.PlaybackState
import com.solstice.model.Song
import com.solstice.network.InnerTubeClient
import com.solstice.player.PlayerController
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    localSongs: List<Song>,
    onScanLocalRequested: () -> Unit
) {
    val playerState by PlayerController.Instance.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Lokal, 1: Online
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showFullscreenPlayer by remember { mutableStateOf(false) }

    val innerTube = remember { InnerTubeClient() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            // Branding Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solstice",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                if (selectedTab == 0) {
                    Button(
                        onClick = onScanLocalRequested,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Scan", tint = PrimaryIndigo)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Folder", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(cornerRadius = 12.dp)
                    .padding(4.dp)
            ) {
                listOf("Lagu Lokal (${localSongs.size})", "Streaming Online").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryIndigo else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar for online tab
            if (selectedTab == 1) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari lagu di YouTube Music...", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryIndigo) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSearching = true
                                        searchResults = innerTube.search(searchQuery)
                                        isSearching = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text("Cari", fontSize = 12.sp)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().liquidGlass(cornerRadius = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Song list
            val displaySongs = if (selectedTab == 0) localSongs else searchResults

            if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else if (displaySongs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 0) "Belum ada lagu lokal ditemukan.\nKlik 'Scan Folder' di atas." else "Ketik judul lagu untuk mencari.",
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(displaySongs) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id && playerState.isPlaying,
                            onClick = {
                                PlayerController.Instance.playSong(song)
                            }
                        )
                    }
                }
            }
        }

        // Mini Player Bar (Floating Liquid Glass)
        if (playerState.currentSong != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .liquidGlass(cornerRadius = 18.dp)
                    .clickable { showFullscreenPlayer = true }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cover = playerState.currentSong?.coverArtUrl
                    if (cover != null) {
                        AsyncImage(
                            model = cover,
                            contentDescription = "Cover",
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = PrimaryIndigo)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playerState.currentSong?.title ?: "",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = playerState.currentSong?.artist ?: "",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { PlayerController.Instance.togglePlayPause() }) {
                        Icon(
                            if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { PlayerController.Instance.next() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                    }
                }
            }
        }

        // Fullscreen Player overlay
        AnimatedVisibility(
            visible = showFullscreenPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullscreenPlayer(
                state = playerState,
                onDismiss = { showFullscreenPlayer = false }
            )
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cover = song.coverArtUrl
        if (cover != null) {
            AsyncImage(
                model = cover,
                contentDescription = song.title,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = if (isPlaying) PrimaryIndigo else Color.White.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 15.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                color = if (isPlaying) PrimaryIndigo else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (song.durationMs > 0) {
            Text(
                text = formatTime(song.durationMs),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}
