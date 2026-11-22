package com.solstice.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solstice.model.PlaybackState
import com.solstice.model.Song
import com.solstice.network.LyricsClient
import com.solstice.player.PlayerController
import kotlinx.coroutines.launch

@Composable
fun FullscreenPlayer(
    state: PlaybackState,
    onDismiss: () -> Unit
) {
    val song = state.currentSong ?: return
    val lyricsClient = remember { LyricsClient() }
    val scope = rememberCoroutineScope()
    var lyrics by remember { mutableStateOf<List<com.solstice.model.LyricLine>>(emptyList()) }
    var plainLyrics by remember { mutableStateOf<String?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }

    LaunchedEffect(song.id) {
        isLoadingLyrics = true
        val res = lyricsClient.getLyrics(song.title, song.artist, song.durationMs / 1000)
        lyrics = res.syncedLyrics
        plainLyrics = res.plainLyrics
        isLoadingLyrics = false
    }

    val activeLyricIdx = remember(state.positionMs, lyrics) {
        lyrics.indexOfLast { it.timeMs <= state.positionMs }.coerceAtLeast(0)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(activeLyricIdx) {
        if (lyrics.isNotEmpty() && activeLyricIdx in lyrics.indices) {
            listState.animateScrollToItem(activeLyricIdx)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground.copy(alpha = 0.96f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    text = "NOW PLAYING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cover Art
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .liquidGlass(cornerRadius = 24.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (song.coverArtUrl != null) {
                    AsyncImage(
                        model = song.coverArtUrl,
                        contentDescription = song.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceDark)
                            .clip(RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎵", fontSize = 64.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Artist
            Text(
                text = song.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Synced Lyrics View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .liquidGlass(cornerRadius = 20.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingLyrics) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                } else if (lyrics.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(lyrics) { idx, line ->
                            val isActive = idx == activeLyricIdx
                            Text(
                                text = line.text,
                                fontSize = if (isActive) 18.sp else 15.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) PrimaryIndigo else Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        PlayerController.Instance.seekTo(line.timeMs)
                                    }
                            )
                        }
                    }
                } else if (!plainLyrics.isNullOrBlank()) {
                    Text(
                        text = plainLyrics ?: "",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Lirik tidak ditemukan",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider
            val progress = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs.toFloat() else 0f
            Slider(
                value = progress.coerceIn(0f, 1f),
                onValueChange = { frac ->
                    PlayerController.Instance.seekTo((frac * state.durationMs).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = PrimaryIndigo,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Time Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(state.positionMs), fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                Text(formatTime(state.durationMs), fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { PlayerController.Instance.previous() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                FloatingActionButton(
                    onClick = { PlayerController.Instance.togglePlayPause() },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = { PlayerController.Instance.next() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
