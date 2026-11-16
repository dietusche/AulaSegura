package com.alexanderpardo.aulasegura.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tablas de la base de datos local Room (copia offline de la información académica). */

@Entity(tableName = "estudiantes")
data class EstudianteEntity(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val grado: String,
    val claveHash: String,
    val ultimaSincronizacion: Long,
    val fuente: String
)

@Entity(tableName = "notas")
data class NotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoEstudiante: String,
    val materia: String,
    val periodo: Int,
    val nota: Double,
    val docente: String
)

@Entity(tableName = "horario")
data class ClaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoEstudiante: String,
    val dia: String,
    val ordenDia: Int,
    val horaInicio: String,
    val horaFin: String,
    val materia: String,
    val salon: String
)

@Entity(tableName = "avisos")
data class AvisoEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val detalle: String,
    val fecha: String,
    val importante: Boolean
)
