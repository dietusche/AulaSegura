package com.alexanderpardo.aulasegura.data.security

import java.security.MessageDigest

/**
 * Verifica la clave sin guardarla nunca en texto plano.
 * Se compara el hash SHA-256 de "codigo:clave" con el hash publicado por la API.
 */
object PasswordHasher {

    fun hash(codigo: String, clave: String): String {
        val entrada = "${codigo.trim()}:$clave".toByteArray(Charsets.UTF_8)
        val bytes = MessageDigest.getInstance("SHA-256").digest(entrada)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verificar(codigo: String, clave: String, hashEsperado: String): Boolean {
        val calculado = hash(codigo, clave).toByteArray(Charsets.UTF_8)
        val esperado = hashEsperado.lowercase().toByteArray(Charsets.UTF_8)
        // Comparación en tiempo constante (evita ataques de tiempo)
        return MessageDigest.isEqual(calculado, esperado)
    }
}
