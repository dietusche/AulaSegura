package com.alexanderpardo.aulasegura.data.remote

import com.alexanderpardo.aulasegura.data.local.AvisoEntity
import com.alexanderpardo.aulasegura.data.local.ClaseEntity
import com.alexanderpardo.aulasegura.data.local.EstudianteEntity
import com.alexanderpardo.aulasegura.data.local.NotaEntity
import org.json.JSONObject

data class PaqueteEstudiante(
    val estudiante: EstudianteEntity,
    val notas: List<NotaEntity>,
    val clases: List<ClaseEntity>
)

/** Convierte el JSON de la API en objetos Kotlin listos para guardar en Room. */
object JsonParser {

    fun estudiante(json: String, fuente: String, ahora: Long): PaqueteEstudiante {
        val raiz = JSONObject(json)
        val codigo = raiz.getString("codigo")
        val estudiante = EstudianteEntity(
            codigo = codigo,
            nombre = raiz.getString("nombre"),
            grado = raiz.getString("grado"),
            claveHash = raiz.getString("claveHash"),
            ultimaSincronizacion = ahora,
            fuente = fuente
        )
        val notasJson = raiz.getJSONArray("notas")
        val notas = (0 until notasJson.length()).map { i ->
            val n = notasJson.getJSONObject(i)
            NotaEntity(
                codigoEstudiante = codigo,
                materia = n.getString("materia"),
                periodo = n.getInt("periodo"),
                nota = n.getDouble("nota"),
                docente = n.optString("docente", "")
            )
        }
        val horarioJson = raiz.getJSONArray("horario")
        val clases = (0 until horarioJson.length()).map { i ->
            val c = horarioJson.getJSONObject(i)
            ClaseEntity(
                codigoEstudiante = codigo,
                dia = c.getString("dia"),
                ordenDia = ordenDelDia(c.getString("dia")),
                horaInicio = c.getString("inicio"),
                horaFin = c.getString("fin"),
                materia = c.getString("materia"),
                salon = c.optString("salon", "")
            )
        }
        return PaqueteEstudiante(estudiante, notas, clases)
    }

    fun avisos(json: String): List<AvisoEntity> {
        val lista = JSONObject(json).getJSONArray("avisos")
        return (0 until lista.length()).map { i ->
            val a = lista.getJSONObject(i)
            AvisoEntity(
                id = a.getString("id"),
                titulo = a.getString("titulo"),
                detalle = a.getString("detalle"),
                fecha = a.getString("fecha"),
                importante = a.optBoolean("importante", false)
            )
        }
    }

    private fun ordenDelDia(dia: String): Int = when (dia.lowercase()) {
        "lunes" -> 1
        "martes" -> 2
        "miércoles", "miercoles" -> 3
        "jueves" -> 4
        "viernes" -> 5
        "sábado", "sabado" -> 6
        else -> 7
    }
}
