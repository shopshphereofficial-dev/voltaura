package com.voltaura.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF00E5FF)
val Violet = Color(0xFF7C4DFF)
val Pink = Color(0xFFFF4DA6)
val Bg = Color(0xFF070A16)
val Surface1 = Color(0xFF11162B)
val Surface2 = Color(0xFF1A2140)
val TextMain = Color(0xFFEAF0FF)
val TextDim = Color(0xFF9AA6C7)

private val scheme = darkColorScheme(
    primary = Cyan,
    secondary = Violet,
    tertiary = Pink,
    background = Bg,
    surface = Surface1,
    onPrimary = Color(0xFF00121A),
    onSecondary = Color.White,
    onBackground = TextMain,
    onSurface = TextMain,
)

@Composable
fun VoltAuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
