package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.RequestEntity
import com.example.data.repository.RequestRepository
import com.example.data.api.GeminiArboristClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Data class representing neighborhood anniversaries of Tarija
data class BarrioAnniversary(
    val barrio: String,
    val distrito: String,
    val fecha: String, // MM-DD
    val descripcion: String
)

class PruningViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = RequestRepository(db.requestDao())

    // Simulated Role
    var userRole by mutableStateOf("Administrador") // "Administrador" (Root), "Técnico" (Restricted), "Vecino" (Public submission)

    // Filter states
    var searchQuery by mutableStateOf("")
    var filterDistrito by mutableStateOf("Todos")
    var filterBarrio by mutableStateOf("Todos")
    var filterUrgencia by mutableStateOf("Todos")
    var filterEstado by mutableStateOf("Todos")

    // All requests direct from repository
    val allRequests: StateFlow<List<RequestEntity>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered requests computed list (reacts to compose state updates automatically)
    val requestsToDisplay: List<RequestEntity>
        get() {
            val list = allRequests.value
            return list.filter { item ->
                val matchQuery = searchQuery.isEmpty() || 
                        item.codigo.contains(searchQuery, ignoreCase = true) ||
                        item.nombreSolicitante.contains(searchQuery, ignoreCase = true) ||
                        item.especie.contains(searchQuery, ignoreCase = true) ||
                        item.barrio.contains(searchQuery, ignoreCase = true) ||
                        item.detalles.contains(searchQuery, ignoreCase = true)
                
                val matchDist = filterDistrito == "Todos" || item.distrito == filterDistrito
                val matchBarr = filterBarrio == "Todos" || item.barrio == filterBarrio
                val matchUrg = filterUrgencia == "Todos" || item.urgencia == filterUrgencia
                val matchEst = filterEstado == "Todos" || item.estado == filterEstado

                matchQuery && matchDist && matchBarr && matchUrg && matchEst
            }
        }

    // Anniversary List for Tarija
    val anniversaries = listOf(
        BarrioAnniversary("San Roque", "D-1", "08-16", "Fiesta patronal y aniversario de la Iglesia Histórica"),
        BarrioAnniversary("El Molino", "D-1", "05-12", "Aniversario de uno de los barrios más tradicionales"),
        BarrioAnniversary("Las Panosas", "D-1", "08-25", "Fundación de las canteras antiguas"),
        BarrioAnniversary("Senac", "D-13", "10-12", "Aniversario del barrio residencial en la zona alta"),
        BarrioAnniversary("Juan XXIII", "D-8", "06-15", "Festividad del Papa bueno y desfile de antorchas"),
        BarrioAnniversary("San Martín", "D-10", "11-11", "Aniversario central y verbena popular"),
        BarrioAnniversary("Tabladita", "D-13", "04-15", "Homenaje de la Batalla de la Tablada departamental"),
        BarrioAnniversary("Aeropuerto", "D-6", "09-24", "Aniversario cerca a la pista del antiguo aeródromo")
    )

    // AI Advisor States
    var aiSpecie by mutableStateOf("")
    var aiDetalles by mutableStateOf("")
    var aiSelectedUrgencia by mutableStateOf("Baja")
    var aiBarrio by mutableStateOf("San Roque")
    var aiDistrito by mutableStateOf("D-1")
    
    var aiLoading by mutableStateOf(false)
    var aiResponse by mutableStateOf<String?>(null)

    init {
        // Pre-populate database with beautiful realistic Tarija data if empty
        viewModelScope.launch {
            allRequests.first().let { currentList ->
                if (currentList.isEmpty()) {
                    insertInitialMockData()
                }
            }
        }
    }

    private suspend fun insertInitialMockData() {
        val mocks = listOf(
            RequestEntity(
                codigo = "PODA-101",
                nombreSolicitante = "María Romero",
                telefonoSolicitante = "72145678",
                distrito = "D-1",
                barrio = "San Roque",
                especie = "Jacarandá",
                urgencia = "Alta",
                tecnicoAsignado = "Ing. Juan Pérez",
                estado = "Pendiente",
                requerimientos = "Plataforma hidráulica, Motosierra de elevación",
                latitud = -21.5309,
                longitud = -64.7314,
                detalles = "Ramas secas de gran peso obstruyen cables de alta tensión de SETAR frente a la plaza. Alto riesgo de chispa eléctrica durante ventiscas.",
                fechaAgendada = "2026-06-15",
                fotos = "poda_jacaranda"
            ),
            RequestEntity(
                codigo = "PODA-102",
                nombreSolicitante = "Carlos Mendoza",
                telefonoSolicitante = "76198765",
                distrito = "D-1",
                barrio = "El Molino",
                especie = "Sauce Llorón",
                urgencia = "Crítica",
                tecnicoAsignado = "Ing. Mariana Arroyo",
                estado = "En Proceso",
                requerimientos = "Grúa telescópica, Motosierra pesada, Cinta de peligro",
                latitud = -21.5284,
                longitud = -64.7360,
                detalles = "Árbol centenario con rajadura severa en el tronco principal tras tempestad de vientos. Inclinado un 35% sobre la calzada pública transitada.",
                fechaAgendada = "2026-05-24",
                fotos = "poda_sauce"
            ),
            RequestEntity(
                codigo = "PODA-103",
                nombreSolicitante = "Lucía Altamirano",
                telefonoSolicitante = "65123456",
                distrito = "D-2",
                barrio = "Las Panosas",
                especie = "Chirimolle",
                urgencia = "Baja",
                tecnicoAsignado = "Ing. Juan Pérez",
                estado = "Terminado",
                requerimientos = "Escalera de extensión, Podadora de mano",
                latitud = -21.5350,
                longitud = -64.7290,
                detalles = "Despeje ornamental de ramas bajas sobre la vereda peatonal. El follaje cubría por completo la visibilidad de la señal de pare.",
                fechaAgendada = "2026-05-10",
                fotos = "poda_chirimolle"
            ),
            RequestEntity(
                codigo = "PODA-104",
                nombreSolicitante = "Alberto Tolay",
                telefonoSolicitante = "73298711",
                distrito = "D-13",
                barrio = "Senac",
                especie = "Pino Radiata",
                urgencia = "Media",
                tecnicoAsignado = "Diego Tolaba (Técnico)",
                estado = "Pendiente",
                requerimientos = "Camión elevador con canastilla, Arneses",
                latitud = -21.5430,
                longitud = -64.7550,
                detalles = "Copa excedida roza y presiona el techo del centro de capacitación de Senac. Solicitud de rebaje preventivo para evitar perforación del tinglado.",
                fechaAgendada = "2026-06-12",
                fotos = "poda_pino"
            ),
            RequestEntity(
                codigo = "PODA-105",
                nombreSolicitante = "Rosa Vásquez",
                telefonoSolicitante = "70211334",
                distrito = "D-8",
                barrio = "Juan XXIII",
                especie = "Ceibo",
                urgencia = "Alta",
                tecnicoAsignado = "Ing. Mariana Arroyo",
                estado = "Pendiente",
                requerimientos = "Ficha técnica ambiental, Cuadrilla de excavación",
                latitud = -21.5220,
                longitud = -64.7180,
                detalles = "Raíces expuestas destruyeron la acera de hormigón y están perforando cañería de agua potable de COSAALT. Requiere poda técnica de raíces y reparación.",
                fechaAgendada = "2026-06-20",
                fotos = "poda_ceibo"
            ),
            RequestEntity(
                codigo = "PODA-106",
                nombreSolicitante = "Fernando Guerrero",
                telefonoSolicitante = "71899122",
                distrito = "D-10",
                barrio = "San Martín",
                especie = "Álamo",
                urgencia = "Baja",
                tecnicoAsignado = "Diego Tolaba (Técnico)",
                estado = "Pendiente",
                requerimientos = "Cuchillas extensoras, Cascos",
                latitud = -21.5390,
                longitud = -64.7150,
                detalles = "Poda ornamental y retiro de hojas secas acumuladas que obstruyen los desagües pluviales de la plazuela San Martín.",
                fechaAgendada = "2026-06-30",
                fotos = "poda_alamo"
            )
        )
        mocks.forEach { repository.insert(it) }
    }

    // CRUD functions
    fun saveRequest(request: RequestEntity) {
        viewModelScope.launch {
            if (request.id == 0) {
                repository.insert(request)
            } else {
                repository.update(request)
            }
        }
    }

    fun deleteRequest(request: RequestEntity) {
        viewModelScope.launch {
            repository.delete(request)
        }
    }

    fun deleteById(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // AI Assessment Trigger
    fun queryAiAssessment() {
        if (aiSpecie.isBlank() || aiDetalles.isBlank()) return
        aiLoading = true
        aiResponse = null
        viewModelScope.launch {
            val response = GeminiArboristClient.getForestryAssessment(
                especie = aiSpecie,
                barrio = aiBarrio,
                distrito = aiDistrito,
                detalles = aiDetalles,
                urgenciaSolicitada = aiSelectedUrgencia
            )
            aiResponse = response
            aiLoading = false
        }
    }

    // Generate simulated auto pruning based on neighborhood anniversary
    fun scheduleAnniversaryPruning(anniversary: BarrioAnniversary) {
        viewModelScope.launch {
            val randomId = (100..999).random()
            val newReq = RequestEntity(
                codigo = "PODA-ANN-$randomId",
                nombreSolicitante = "Secretaría de Ornato (Alcaldía)",
                telefonoSolicitante = "011-Tarija",
                distrito = anniversary.distrito,
                barrio = anniversary.barrio,
                especie = "Varias (Parque Central)",
                urgencia = "Media",
                tecnicoAsignado = "Cuadrilla Especial de Eventos",
                estado = "Pendiente",
                requerimientos = "Kit completo de mantenimiento vecinal",
                latitud = when (anniversary.barrio) {
                    "San Roque" -> -21.5309
                    "El Molino" -> -21.5284
                    "Las Panosas" -> -21.5350
                    "Senac" -> -21.5430
                    "Juan XXIII" -> -21.5220
                    "San Martín" -> -21.5390
                    else -> -21.53 // Centerish
                },
                longitud = when (anniversary.barrio) {
                    "San Roque" -> -64.7314
                    "El Molino" -> -64.7360
                    "Las Panosas" -> -64.7290
                    "Senac" -> -64.7550
                    "Juan XXIII" -> -64.7180
                    "San Martín" -> -64.7150
                    else -> -64.72 // Centerish
                },
                detalles = "Poda preventiva festiva por Aniversario de Barrio ${anniversary.barrio}. Limpieza de alumbrado público, despeje ornamental y arreglo de copas previo al desfile comunal del ${anniversary.fecha}.",
                fechaAgendada = "2026-${anniversary.fecha}",
                fotos = "limpieza_aniversario"
            )
            repository.insert(newReq)
        }
    }
}
