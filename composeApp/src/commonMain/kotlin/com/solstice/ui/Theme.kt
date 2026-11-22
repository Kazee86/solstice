package com.solstice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val PrimaryIndigo = Color(0xFF6366F1)
val DarkBackground = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val GlassBorder = Color(0x33FFFFFF)
val GlassBackground = Color(0x22FFFFFF)

private val SolsticeColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    background = DarkBackground,
    surface = SurfaceDark,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFE2E8F0)
)

@Composable
fun SolsticeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SolsticeColorScheme,
        content = content
    )
}

fun Modifier.liquidGlass(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = GlassBackground,
    borderColor: Color = GlassBorder
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(borderColor, Color.Transparent)
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
