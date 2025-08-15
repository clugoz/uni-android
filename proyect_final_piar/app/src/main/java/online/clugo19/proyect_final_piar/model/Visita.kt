package online.clugo19.proyect_final_piar.model

data class Visita(
    val escuelaId: String,
    val fechaIso: String,       // yyyy-MM-dd
    val horaEntrada: String,    // HH:mm
    val horaSalida: String,     // HH:mm
    val asesoresIds: List<String>
)
