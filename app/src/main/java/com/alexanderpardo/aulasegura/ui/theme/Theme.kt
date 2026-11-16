package com.alexanderpardo.aulasegura.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VerdeInstitucional = Color(0xFF0B4A35)
val VerdeClaro = Color(0xFFE8F3EE)
val Dorado = Color(0xFFB8963E)
val DoradoClaro = Color(0xFFF7F0DE)
val RojoAlerta = Color(0xFFB3362B)
val VerdeAprobado = Color(0xFF1F7A50)

private val Colores = lightColorScheme(
    primary = VerdeInstitucional,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeInstitucional,
    secondary = Dorado,
    onSecondary = Color.White,
    secondaryContainer = DoradoClaro,
    onSecondaryContainer = Color(0xFF4A3B12),
    error = RojoAlerta,
    background = Color(0xFFFAFCFB),
    surface = Color.White
)

@Composable
fun AulaSeguraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, content = content)
}
