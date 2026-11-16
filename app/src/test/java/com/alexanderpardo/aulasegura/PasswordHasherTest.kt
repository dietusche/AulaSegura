package com.alexanderpardo.aulasegura

import com.alexanderpardo.aulasegura.data.security.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias del control de autenticación (se ejecutan sin emulador). */
class PasswordHasherTest {

    private val hashDe1101 = "984ea125596171b560b2f18dcbb98a4df34bfae05176f68a5bb65bb441c7a585"

    @Test
    fun hashCoincideConElPublicadoEnLaApi() {
        assertEquals(hashDe1101, PasswordHasher.hash("1101", "Santander2026"))
    }

    @Test
    fun claveCorrectaEsAceptada() {
        assertTrue(PasswordHasher.verificar("1101", "Santander2026", hashDe1101))
    }

    @Test
    fun claveIncorrectaEsRechazada() {
        assertFalse(PasswordHasher.verificar("1101", "clave-equivocada", hashDe1101))
    }

    @Test
    fun codigoDeOtroEstudianteNoSirveConElMismoHash() {
        assertFalse(PasswordHasher.verificar("1102", "Santander2026", hashDe1101))
    }
}
