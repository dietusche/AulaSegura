package com.alexanderpardo.aulasegura.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderpardo.aulasegura.data.AulaRepository
import com.alexanderpardo.aulasegura.data.ResultadoLogin
import com.alexanderpardo.aulasegura.data.local.AvisoEntity
import com.alexanderpardo.aulasegura.data.local.ClaseEntity
import com.alexanderpardo.aulasegura.data.local.EstudianteEntity
import com.alexanderpardo.aulasegura.data.local.NotaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoUi(
    val iniciando: Boolean = true,
    val cargando: Boolean = false,
    val codigo: String? = null,
    val mensaje: String? = null
)

/** ViewModel (patrón MVVM): guarda el estado de la pantalla y habla con el repositorio. */
class AulaViewModel(app: Application) : AndroidViewModel(app) {

    private val repositorio = AulaRepository(app)

    private val _estado = MutableStateFlow(EstadoUi())
    val estado: StateFlow<EstadoUi> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = repositorio.tokens.leerSesion()
            _estado.value = EstadoUi(iniciando = false, codigo = sesion?.codigoEstudiante)
        }
    }

    fun iniciarSesion(codigo: String, clave: String) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            when (val resultado = repositorio.iniciarSesion(codigo, clave)) {
                is ResultadoLogin.Exito -> _estado.value = EstadoUi(
                    iniciando = false,
                    codigo = resultado.codigo,
                    mensaje = "Bienvenido. Datos cargados desde: ${resultado.fuente}"
                )
                is ResultadoLogin.Error -> _estado.update {
                    it.copy(cargando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }

    fun refrescar() {
        val codigo = _estado.value.codigo ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val mensaje = repositorio.refrescar(codigo)
            _estado.update { it.copy(cargando = false, mensaje = mensaje) }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repositorio.tokens.cerrarSesion()
            _estado.value = EstadoUi(iniciando = false)
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    fun estudiante(codigo: String): Flow<EstudianteEntity?> = repositorio.observarEstudiante(codigo)
    fun notas(codigo: String): Flow<List<NotaEntity>> = repositorio.observarNotas(codigo)
    fun horario(codigo: String): Flow<List<ClaseEntity>> = repositorio.observarHorario(codigo)
    fun avisos(): Flow<List<AvisoEntity>> = repositorio.observarAvisos()
}
