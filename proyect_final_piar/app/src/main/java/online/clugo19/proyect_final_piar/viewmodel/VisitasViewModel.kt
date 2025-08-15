package online.clugo19.proyect_final_piar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import online.clugo19.proyect_final_piar.model.Visita

class VisitasViewModel : ViewModel() {

    private val TAG = "VisitasVM"

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Catálogos
    private val _escuelas = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val escuelas: StateFlow<List<Pair<String, String>>> = _escuelas.asStateFlow()

    private val _asesores = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val asesores: StateFlow<List<Pair<String, String>>> = _asesores.asStateFlow()

    // Loading por catálogo
    private val escLoading = MutableStateFlow(true)
    private val aseLoading = MutableStateFlow(true)

    // Loading combinado para la UI (el historial no bloquea el formulario)
    val loading: StateFlow<Boolean> =
        combine(escLoading, aseLoading) { e, a -> e || a }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Errores para Snackbar
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Historial de visitas (ya listo para pintar)
    data class VisitaUI(
        val id: String,
        val escuelaNombre: String,
        val fechaIso: String,
        val asesoresResumen: String
    )

    private val _visitas = MutableStateFlow<List<VisitaUI>>(emptyList())
    val visitas: StateFlow<List<VisitaUI>> = _visitas.asStateFlow()

    // Listeners Firestore
    private var escuelasListener: ListenerRegistration? = null
    private var asesoresListener: ListenerRegistration? = null
    private var visitasListener: ListenerRegistration? = null

    init {
        Log.d(TAG, "Init ViewModel. uid=${auth.currentUser?.uid}")

        suscribirEscuelas()
        suscribirAsesores()
        suscribirVisitas()
    }

    private fun suscribirEscuelas() {
        escLoading.value = true
        Log.d(TAG, "Suscribiendo ESCUELAS…")

        // Quita whereEqualTo("activo", true) si tus docs no tienen ese campo
        escuelasListener = db.collection("escuelas")
            //.whereEqualTo("activo", true)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    _error.value = "Escuelas: ${e.localizedMessage}"
                    escLoading.value = false
                    val fe = e as? FirebaseFirestoreException
                    Log.e(TAG, "Snapshot ESCUELAS error code=${fe?.code} msg=${e.message}", e)
                    return@addSnapshotListener
                }

                val size = snap?.size() ?: 0
                Log.d(TAG, "Snapshot ESCUELAS OK. docs=$size")

                val lista = snap?.documents.orEmpty().mapIndexed { i, d ->
                    val id = d.id
                    val nombre = d.getString("nombre")
                    val activo = d.getBoolean("activo")
                    Log.d(TAG, "Escuela[$i] id=$id nombre=$nombre activo=$activo")
                    id to (nombre ?: "Sin nombre")
                }.sortedBy { it.second.lowercase() }

                _escuelas.value = lista
                escLoading.value = false
                Log.d(TAG, "Escuelas cargadas: ${lista.size}")
            }
    }

    private fun suscribirAsesores() {
        aseLoading.value = true
        Log.d(TAG, "Suscribiendo ASESORES…")

        asesoresListener = db.collection("asesores")
            //.whereEqualTo("activo", true)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    _error.value = "Asesores: ${e.localizedMessage}"
                    aseLoading.value = false
                    val fe = e as? FirebaseFirestoreException
                    Log.e(TAG, "Snapshot ASESORES error code=${fe?.code} msg=${e.message}", e)
                    return@addSnapshotListener
                }

                val size = snap?.size() ?: 0
                Log.d(TAG, "Snapshot ASESORES OK. docs=$size")

                val lista = snap?.documents.orEmpty().mapIndexed { i, d ->
                    val id = d.id
                    val nombre = d.getString("nombre")
                    val activo = d.getBoolean("activo")
                    Log.d(TAG, "Asesor[$i] id=$id nombre=$nombre activo=$activo")
                    id to (nombre ?: "Sin nombre")
                }.sortedBy { it.second.lowercase() }

                _asesores.value = lista
                aseLoading.value = false
                Log.d(TAG, "Asesores cargados: ${lista.size}")
            }
    }

    private fun suscribirVisitas() {
        Log.d(TAG, "Suscribiendo HISTORIAL…")

        // Si quieres filtrar solo por el usuario actual, descomenta la línea del where:
        // y crea el índice compuesto si Firestore lo pide (uidCreador + createdAt).
        visitasListener = db.collection("visitas")
            //.whereEqualTo("uidCreador", auth.currentUser?.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    _error.value = "Historial: ${e.localizedMessage}"
                    val fe = e as? FirebaseFirestoreException
                    Log.e(TAG, "Snapshot HISTORIAL error code=${fe?.code} msg=${e.message}", e)
                    return@addSnapshotListener
                }

                val lista = snap?.documents.orEmpty().map { d ->
                    val id = d.id
                    val escuelaNombre = d.getString("escuelaNombre") ?: "—"
                    val fechaIso = d.getString("fecha") ?: "—"
                    val asesoresNombres = (d.get("asesoresNombres") as? List<*>)?.filterIsInstance<String>().orEmpty()
                    val asesoresResumen = if (asesoresNombres.isEmpty()) "—" else asesoresNombres.joinToString()
                    VisitaUI(
                        id = id,
                        escuelaNombre = escuelaNombre,
                        fechaIso = fechaIso,
                        asesoresResumen = asesoresResumen
                    )
                }

                _visitas.value = lista
                Log.d(TAG, "Historial cargado: ${lista.size} visitas")
            }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared(): removiendo listeners")
        escuelasListener?.remove()
        asesoresListener?.remove()
        visitasListener?.remove()
        super.onCleared()
    }

    /**
     * Guarda una visita.
     * NOTA: mantenemos tu data class Visita existente y pasamos nombres denormalizados aparte.
     */
    fun crearVisita(
        v: Visita,
        escuelaNombre: String,
        asesoresNombres: List<String>,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando visita: $v (escuelaNombre=$escuelaNombre, asesoresNombres=$asesoresNombres)")
                val data = hashMapOf(
                    "escuelaId" to v.escuelaId,
                    "fecha" to v.fechaIso,
                    "horaEntrada" to v.horaEntrada,
                    "horaSalida" to v.horaSalida,
                    "asesoresIds" to v.asesoresIds,
                    "uidCreador" to auth.currentUser?.uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    // Denormalizados para el historial
                    "escuelaNombre" to escuelaNombre,
                    "asesoresNombres" to asesoresNombres
                )
                db.collection("visitas").add(data).await()
                Log.d(TAG, "Visita creada OK")
                onDone(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error creando visita", e)
                _error.value = "No se pudo guardar: ${e.localizedMessage}"
                onDone(false)
            }
        }
    }
}
