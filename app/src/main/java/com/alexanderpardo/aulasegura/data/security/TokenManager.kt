package com.alexanderpardo.aulasegura.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.sesionDataStore by preferencesDataStore(name = "sesion_segura")

data class Sesion(val codigoEstudiante: String, val token: String, val expiraEn: Long)

/**
 * Control B: almacenamiento seguro del token de sesión.
 * El token se cifra con AES-256-GCM usando una clave que vive en el Android Keystore
 * (nunca sale del hardware seguro) y solo el texto cifrado se guarda en DataStore.
 */
class TokenManager(private val context: Context) {

    private val alias = "aulasegura_token_key"
    private val claveToken = stringPreferencesKey("token_cifrado")
    private val claveIv = stringPreferencesKey("token_iv")
    private val claveCodigo = stringPreferencesKey("codigo_estudiante")
    private val claveExpira = longPreferencesKey("expira_en")

    private fun obtenerClaveKeystore(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existente = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        if (existente != null) return existente.secretKey

        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val especificacion = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generador.init(especificacion)
        return generador.generateKey()
    }

    /** Crea un token de sesión aleatorio, lo cifra y lo guarda. Dura 8 horas. */
    suspend fun guardarSesion(codigo: String, duracionMs: Long = 8L * 60 * 60 * 1000): Sesion {
        val token = UUID.randomUUID().toString()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClaveKeystore())
        val cifrado = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val expira = System.currentTimeMillis() + duracionMs

        context.sesionDataStore.edit { prefs ->
            prefs[claveToken] = Base64.encodeToString(cifrado, Base64.NO_WRAP)
            prefs[claveIv] = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            prefs[claveCodigo] = codigo
            prefs[claveExpira] = expira
        }
        return Sesion(codigo, token, expira)
    }

    /** Devuelve la sesión si existe y no ha vencido; si no, la borra. */
    suspend fun leerSesion(): Sesion? {
        val prefs = context.sesionDataStore.data.first()
        val tokenCifrado = prefs[claveToken] ?: return null
        val iv = prefs[claveIv] ?: return null
        val codigo = prefs[claveCodigo] ?: return null
        val expira = prefs[claveExpira] ?: return null

        if (System.currentTimeMillis() > expira) {
            cerrarSesion()
            return null
        }
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            cipher.init(Cipher.DECRYPT_MODE, obtenerClaveKeystore(), spec)
            val bytes = cipher.doFinal(Base64.decode(tokenCifrado, Base64.NO_WRAP))
            Sesion(codigo, String(bytes, Charsets.UTF_8), expira)
        } catch (e: Exception) {
            cerrarSesion()
            null
        }
    }

    suspend fun cerrarSesion() {
        context.sesionDataStore.edit { it.clear() }
    }
}
