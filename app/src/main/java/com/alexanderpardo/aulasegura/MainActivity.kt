package com.alexanderpardo.aulasegura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alexanderpardo.aulasegura.ui.screens.AulaApp
import com.alexanderpardo.aulasegura.ui.theme.AulaSeguraTheme

/** Punto de entrada: Android ejecuta onCreate y setContent dibuja la interfaz con Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AulaSeguraTheme {
                AulaApp()
            }
        }
    }
}
