package com.alexanderpardo.aulasegura.data.remote

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Cliente de la API REST. Descarga JSON por HTTPS (GET).
 * Si no hay internet, el repositorio puede usar la copia de demostración incluida en la app.
 */
class ApiClient(private val context: Context) {

    /** Descarga desde la nube. Lanza excepción si no hay conexión o el recurso no existe. */
    suspend fun descargarNube(ruta: String): String = withContext(Dispatchers.IO) {
        val direccion = ApiConfig.BASE_URL + ruta
        require(direccion.startsWith("https://")) { "Solo se permiten conexiones HTTPS" }
        val conexion = URL(direccion).openConnection() as HttpsURLConnection
        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 6000
            conexion.readTimeout = 6000
            conexion.setRequestProperty("Accept", "application/json")
            if (conexion.responseCode != HttpsURLConnection.HTTP_OK) {
                throw IOException("Respuesta HTTP ${conexion.responseCode}")
            }
            conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conexion.disconnect()
        }
    }

    /** Lee la copia de demostración empaquetada en app/src/main/assets/api/v1/. */
    suspend fun leerDemo(ruta: String): String = withContext(Dispatchers.IO) {
        context.assets.open("api/v1/$ruta").bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
