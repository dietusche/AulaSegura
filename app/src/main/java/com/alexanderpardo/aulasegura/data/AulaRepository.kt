package com.alexanderpardo.aulasegura.data

import android.content.Context
import com.alexanderpardo.aulasegura.data.local.AppDatabase
import com.alexanderpardo.aulasegura.data.local.AvisoEntity
import com.alexanderpardo.aulasegura.data.local.ClaseEntity
import com.alexanderpardo.aulasegura.data.local.EstudianteEntity
import com.alexanderpardo.aulasegura.data.local.NotaEntity
import com.alexanderpardo.aulasegura.data.remote.ApiClient
import com.alexanderpardo.aulasegura.data.remote.JsonParser
import com.alexanderpardo.aulasegura.data.security.PasswordHasher
import com.alexanderpardo.aulasegura.data.security.TokenManager
import kotlinx.coroutines.flow.Flow

sealed class ResultadoLogin {
    data class Exito(val codigo: String, val fuente: String) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}

/**
 * Única fuente de verdad (patrón Repository, estrategia Offline-First):
 * 1) intenta la nube, 2) si no hay red usa la copia guardada en Room,
 * 3) si es la primera vez y no hay red, usa la copia de demostración.
 */
class AulaRepository(context: Context) {

    private val dao = AppDatabase.obtener(context).aulaDao()
    private val api = ApiClient(context)
    val tokens = TokenManager(context)

    fun observarEstudiante(codigo: String): Flow<EstudianteEntity?> = dao.observarEstudiante(codigo)
    fun observarNotas(codigo: String): Flow<List<NotaEntity>> = dao.observarNotas(codigo)
    fun observarHorario(codigo: String): Flow<List<ClaseEntity>> = dao.observarHorario(codigo)
    fun observarAvisos(): Flow<List<AvisoEntity>> = dao.observarAvisos()

    suspend fun iniciarSesion(codigoEscrito: String, clave: String): ResultadoLogin {
        val codigo = codigoEscrito.trim()
        if (codigo.isEmpty() || clave.isEmpty()) {
            return ResultadoLogin.Error("Escribe tu código y tu clave.")
        }

        // 1) Nube
        val jsonNube = try { api.descargarNube("estudiantes/$codigo.json") } catch (e: Exception) { null }
        if (jsonNube != null) return validarYGuardar(codigo, clave, jsonNube, FUENTE_NUBE)

        // 2) Copia local guardada (sin conexión)
        val local = dao.estudiante(codigo)
        if (local != null) {
            return if (PasswordHasher.verificar(codigo, clave, local.claveHash)) {
                tokens.guardarSesion(codigo)
                ResultadoLogin.Exito(codigo, FUENTE_LOCAL)
            } else {
                ResultadoLogin.Error("Código o clave incorrectos.")
            }
        }

        // 3) Copia de demostración incluida en la app
        val jsonDemo: String? = try { api.leerDemo("estudiantes/$codigo.json") } catch (e: Exception) { null }
        if (jsonDemo == null) {
            return ResultadoLogin.Error("Código no encontrado o sin conexión a internet.")
        }
        return validarYGuardar(codigo, clave, jsonDemo, FUENTE_DEMO)
    }

    private suspend fun validarYGuardar(
        codigo: String,
        clave: String,
        json: String,
        fuente: String
    ): ResultadoLogin {
        val paquete = try {
            JsonParser.estudiante(json, fuente, System.currentTimeMillis())
        } catch (e: Exception) {
            return ResultadoLogin.Error("La información recibida no es válida.")
        }
        if (!PasswordHasher.verificar(codigo, clave, paquete.estudiante.claveHash)) {
            return ResultadoLogin.Error("Código o clave incorrectos.")
        }
        val avisos = obtenerAvisos(fuente)
        dao.reemplazarDatos(paquete.estudiante, paquete.notas, paquete.clases, avisos)
        tokens.guardarSesion(codigo)
        return ResultadoLogin.Exito(codigo, fuente)
    }

    private suspend fun obtenerAvisos(fuente: String): List<AvisoEntity>? = try {
        val json = if (fuente == FUENTE_NUBE) api.descargarNube("avisos.json") else api.leerDemo("avisos.json")
        JsonParser.avisos(json)
    } catch (e: Exception) {
        null
    }

    /** Botón "Actualizar": solo consulta la nube; si falla, se conservan los datos guardados. */
    suspend fun refrescar(codigo: String): String = try {
        val json = api.descargarNube("estudiantes/$codigo.json")
        val paquete = JsonParser.estudiante(json, FUENTE_NUBE, System.currentTimeMillis())
        dao.reemplazarDatos(paquete.estudiante, paquete.notas, paquete.clases, obtenerAvisos(FUENTE_NUBE))
        "Datos actualizados desde la nube."
    } catch (e: Exception) {
        "Sin conexión con la nube: se muestran los datos guardados en el teléfono."
    }

    companion object {
        const val FUENTE_NUBE = "Nube (API en GitHub Pages)"
        const val FUENTE_LOCAL = "Copia guardada en el teléfono"
        const val FUENTE_DEMO = "Copia de demostración"
    }
}
