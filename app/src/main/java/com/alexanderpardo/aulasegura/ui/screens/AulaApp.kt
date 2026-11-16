package com.alexanderpardo.aulasegura.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexanderpardo.aulasegura.ui.AulaViewModel
import com.alexanderpardo.aulasegura.ui.EstadoUi

/** Decide qué pantalla mostrar: carga, inicio de sesión o la app principal. */
@Composable
fun AulaApp(vm: AulaViewModel = viewModel()) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val codigo = estado.codigo
    when {
        estado.iniciando -> PantallaCarga()
        codigo == null -> LoginScreen(
            cargando = estado.cargando,
            mensaje = estado.mensaje,
            onIngresar = vm::iniciarSesion
        )
        else -> PantallaPrincipal(vm = vm, codigo = codigo, estado = estado)
    }
}

@Composable
fun PantallaCarga() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(vm: AulaViewModel, codigo: String, estado: EstadoUi) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    val estudiante by remember(codigo) { vm.estudiante(codigo) }
        .collectAsStateWithLifecycle(initialValue = null)
    val notas by remember(codigo) { vm.notas(codigo) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val horario by remember(codigo) { vm.horario(codigo) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val avisos by remember { vm.avisos() }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(estado.mensaje) {
        val mensaje = estado.mensaje
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            vm.mensajeMostrado()
        }
    }

    val pestanas = listOf(
        "Inicio" to Icons.Filled.Home,
        "Notas" to Icons.Filled.Star,
        "Horario" to Icons.Filled.DateRange
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pestana == 0) "AulaSegura" else pestanas[pestana].first) },
                actions = {
                    IconButton(onClick = vm::refrescar, enabled = !estado.cargando) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar desde la nube")
                    }
                    IconButton(onClick = vm::cerrarSesion) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                pestanas.forEachIndexed { indice, (titulo, icono) ->
                    NavigationBarItem(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        icon = { Icon(icono, contentDescription = null) },
                        label = { Text(titulo) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            when (pestana) {
                0 -> InicioTab(estudiante = estudiante, notas = notas, avisos = avisos)
                1 -> NotasTab(notas = notas)
                else -> HorarioTab(clases = horario)
            }
        }
    }
}
