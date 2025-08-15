package online.clugo19.proyect_final_piar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
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

    // Loading por colección
    private val escLoading = MutableStateFlow(true)
    private val aseLoading = MutableStateFlow(true)

    // Loading combinado para la UI
    val loading: StateFlow<Boolean> =
        combine(escLoading, aseLoading) { e, a -> e || a }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Errores para Snackbar
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Listeners Firestore
    private var escuelasListener: ListenerRegistration? = null
    private var asesoresListener: ListenerRegistration? = null

    init {
        Log.d(TAG, "Init ViewModel. uid=${auth.currentUser?.uid}")
        // Persistencia local ya viene habilitada por defecto en SDKs recientes.

        suscribirEscuelas()
        suscribirAsesores()
    }

    private fun suscribirEscuelas() {
        escLoading.value = true
        Log.d(TAG, "Suscribiendo ESCUELAS…")

        // ⚠️ Si tus docs NO tienen 'activo', no uses whereEqualTo
        escuelasListener = db.collection("escuelas")
            // .whereEqualTo("activo", true)
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
            // .whereEqualTo("activo", true) // habilítalo si TODOS tienen este campo
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

    override fun onCleared() {
        Log.d(TAG, "onCleared(): removiendo listeners")
        escuelasListener?.remove()
        asesoresListener?.remove()
        super.onCleared()
    }

    fun crearVisita(v: Visita, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando visita: $v")
                val data = hashMapOf(
                    "escuelaId" to v.escuelaId,
                    "fecha" to v.fechaIso,
                    "horaEntrada" to v.horaEntrada,
                    "horaSalida" to v.horaSalida,
                    "asesoresIds" to v.asesoresIds,
                    "uidCreador" to auth.currentUser?.uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
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
