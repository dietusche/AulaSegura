package com.alexanderpardo.aulasegura.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alexanderpardo.aulasegura.data.local.AvisoEntity
import com.alexanderpardo.aulasegura.data.local.ClaseEntity
import com.alexanderpardo.aulasegura.data.local.EstudianteEntity
import com.alexanderpardo.aulasegura.data.local.NotaEntity
import com.alexanderpardo.aulasegura.ui.theme.Dorado
import com.alexanderpardo.aulasegura.ui.theme.RojoAlerta
import com.alexanderpardo.aulasegura.ui.theme.VerdeAprobado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeCo: Locale = Locale.forLanguageTag("es-CO")

private fun decimal(valor: Double): String = String.format(localeCo, "%.1f", valor)

private fun fecha(milis: Long): String =
    SimpleDateFormat("dd/MM/yyyy h:mm a", localeCo).format(Date(milis))

/** Escala de valoración habitual en Colombia (Decreto 1290 de 2009). */
private fun desempeno(nota: Double): Pair<String, Color> = when {
    nota >= 4.6 -> "Superior" to VerdeAprobado
    nota >= 4.0 -> "Alto" to VerdeAprobado
    nota >= 3.0 -> "Básico" to Dorado
    else -> "Bajo" to RojoAlerta
}

// ---------------------------------------------------------------- INICIO

@Composable
fun InicioTab(estudiante: EstudianteEntity?, notas: List<NotaEntity>, avisos: List<AvisoEntity>) {
    val periodoActual = notas.maxOfOrNull { it.periodo }
    val notasPeriodo = notas.filter { it.periodo == periodoActual }
    val promedio = if (notasPeriodo.isEmpty()) null else notasPeriodo.map { it.nota }.average()
    val enRiesgo = notasPeriodo.count { it.nota < 3.0 }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hola,", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        estudiante?.nombre ?: "Cargando…",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Grado ${estudiante?.grado ?: "-"} · Código ${estudiante?.codigo ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Indicador(
                    titulo = "Promedio periodo ${periodoActual ?: "-"}",
                    valor = promedio?.let { decimal(it) } ?: "-",
                    color = promedio?.let { desempeno(it).second } ?: Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Indicador(
                    titulo = "Materias en riesgo",
                    valor = enRiesgo.toString(),
                    color = if (enRiesgo > 0) RojoAlerta else VerdeAprobado,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sincronización", fontWeight = FontWeight.Bold)
                    Text(
                        "Origen: ${estudiante?.fuente ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Última actualización: ${estudiante?.let { fecha(it.ultimaSincronizacion) } ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Los datos quedan guardados en el teléfono para consultarlos sin internet.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Text(
                "Avisos del colegio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (avisos.isEmpty()) {
            item { Text("No hay avisos guardados.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(avisos, key = { it.id }) { aviso -> AvisoCard(aviso) }
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text(
                valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AvisoCard(aviso: AvisoEntity) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (aviso.importante) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                if (aviso.importante) Icons.Filled.Warning else Icons.Filled.Notifications,
                contentDescription = null,
                tint = if (aviso.importante) Dorado else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(aviso.titulo, fontWeight = FontWeight.Bold)
                Text(aviso.detalle, style = MaterialTheme.typography.bodySmall)
                Text(
                    aviso.fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------- NOTAS

@Composable
fun NotasTab(notas: List<NotaEntity>) {
    val porPeriodo = notas.groupBy { it.periodo }.toList().sortedByDescending { it.first }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (notas.isEmpty()) {
            item { Text("Aún no hay notas guardadas.") }
        }
        porPeriodo.forEach { (periodo, lista) ->
            item(key = "periodo-$periodo") {
                Text(
                    "Periodo $periodo · Promedio ${decimal(lista.map { it.nota }.average())}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(lista, key = { "nota-${it.id}" }) { nota -> NotaFila(nota) }
        }
    }
}

@Composable
private fun NotaFila(nota: NotaEntity) {
    val (etiqueta, color) = desempeno(nota.nota)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(nota.materia, fontWeight = FontWeight.SemiBold)
                Text(
                    nota.docente,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(color = color, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        decimal(nota.nota),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

// ---------------------------------------------------------------- HORARIO

@Composable
fun HorarioTab(clases: List<ClaseEntity>) {
    val porDia = clases.groupBy { it.dia }.toList()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (clases.isEmpty()) {
            item { Text("Aún no hay horario guardado.") }
        }
        porDia.forEach { (dia, lista) ->
            item(key = "dia-$dia") {
                Text(
                    dia,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(lista, key = { "clase-${it.id}" }) { clase ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${clase.horaInicio}\n${clase.horaFin}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(56.dp)
                        )
                        Column {
                            Text(clase.materia, fontWeight = FontWeight.SemiBold)
                            Text(clase.salon, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
