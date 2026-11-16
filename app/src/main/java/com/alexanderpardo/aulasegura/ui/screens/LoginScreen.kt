package com.alexanderpardo.aulasegura.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexanderpardo.aulasegura.ui.theme.Dorado

@Composable
fun LoginScreen(
    cargando: Boolean,
    mensaje: String?,
    onIngresar: (String, String) -> Unit
) {
    var codigo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var verClave by rememberSaveable { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Dorado,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "AulaSegura",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Tu información académica de grado 11, incluso sin conexión",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = codigo,
                onValueChange = { nuevo -> codigo = nuevo.filter { it.isDigit() }.take(6) },
                label = { Text("Código estudiantil") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Clave") },
                singleLine = true,
                visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { verClave = !verClave }) {
                        Text(if (verClave) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (mensaje != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onIngresar(codigo, clave) },
                enabled = !cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Ingresar")
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Usuarios de prueba (datos ficticios)", fontWeight = FontWeight.Bold)
                    Text(
                        "Códigos 1101, 1102 o 1103 · Clave: Santander2026",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Conexión HTTPS · sesión cifrada con Android Keystore",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
