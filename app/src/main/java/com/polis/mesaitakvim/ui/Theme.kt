package com.polis.mesaitakvim.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val Light = lightColorScheme(
    primary = Color(0xFF1A237E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFFE53935),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFF7F8FC),
    surfaceVariant = Color(0xFFE6E8F2)
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    onPrimary = Color(0xFF0B1A6E),
    primaryContainer = Color(0xFF283593),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFFF8A80)
)

@Composable
fun MesaiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}

/** Arka plan rengine göre okunaklı yazı rengi */
fun textOn(bg: Color): Color = if (bg.luminance() > 0.45f) Color(0xFF1B1B1F) else Color.White

val LeaveColor = Color(0xFFB0BEC5)
