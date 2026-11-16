package com.alexanderpardo.aulasegura.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EstudianteEntity::class, NotaEntity::class, ClaseEntity::class, AvisoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun aulaDao(): AulaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aulasegura.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}
