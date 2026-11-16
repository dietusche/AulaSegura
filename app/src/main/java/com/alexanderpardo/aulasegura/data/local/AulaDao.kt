package com.alexanderpardo.aulasegura.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** Consultas a la base de datos local. Los Flow actualizan la pantalla automáticamente. */
@Dao
abstract class AulaDao {

    @Query("SELECT * FROM estudiantes WHERE codigo = :codigo LIMIT 1")
    abstract suspend fun estudiante(codigo: String): EstudianteEntity?

    @Query("SELECT * FROM estudiantes WHERE codigo = :codigo LIMIT 1")
    abstract fun observarEstudiante(codigo: String): Flow<EstudianteEntity?>

    @Query("SELECT * FROM notas WHERE codigoEstudiante = :codigo ORDER BY periodo DESC, materia ASC")
    abstract fun observarNotas(codigo: String): Flow<List<NotaEntity>>

    @Query("SELECT * FROM horario WHERE codigoEstudiante = :codigo ORDER BY ordenDia ASC, horaInicio ASC")
    abstract fun observarHorario(codigo: String): Flow<List<ClaseEntity>>

    @Query("SELECT * FROM avisos ORDER BY fecha DESC")
    abstract fun observarAvisos(): Flow<List<AvisoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarEstudiante(estudiante: EstudianteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarNotas(notas: List<NotaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarHorario(clases: List<ClaseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarAvisos(avisos: List<AvisoEntity>)

    @Query("DELETE FROM notas WHERE codigoEstudiante = :codigo")
    abstract suspend fun borrarNotas(codigo: String)

    @Query("DELETE FROM horario WHERE codigoEstudiante = :codigo")
    abstract suspend fun borrarHorario(codigo: String)

    @Query("DELETE FROM avisos")
    abstract suspend fun borrarAvisos()

    /** Reemplaza todos los datos de un estudiante en una sola transacción. */
    @Transaction
    open suspend fun reemplazarDatos(
        estudiante: EstudianteEntity,
        notas: List<NotaEntity>,
        clases: List<ClaseEntity>,
        avisos: List<AvisoEntity>?
    ) {
        guardarEstudiante(estudiante)
        borrarNotas(estudiante.codigo)
        guardarNotas(notas)
        borrarHorario(estudiante.codigo)
        guardarHorario(clases)
        if (avisos != null) {
            borrarAvisos()
            guardarAvisos(avisos)
        }
    }
}
