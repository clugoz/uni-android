package online.clugo19.proyect_final_piar.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import online.clugo19.proyect_final_piar.model.Visita
import online.clugo19.proyect_final_piar.viewmodel.VisitasViewModel
import java.util.Calendar

@Composable
fun VisitasScreen(
    viewModel: VisitasViewModel,
    onSaved: () -> Unit = {}
) {
    val escuelas by viewModel.escuelas.collectAsState()
    val asesores by viewModel.asesores.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val visitas by viewModel.visitas.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tab state
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Formulario", "Historial")

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            scope.launch { snackbar.showSnackbar(error!!) }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> FormularioVisita(
                    viewModel = viewModel,
                    escuelas = escuelas,
                    asesores = asesores,
                    loading = loading,
                    snackbar = snackbar,
                    onSaved = onSaved
                )

                1 -> HistorialVisitas(visitas = visitas)
            }
        }
    }
}

/* =========================
   FORMULARIO DE VISITA
   ========================= */

@Composable
private fun FormularioVisita(
    viewModel: VisitasViewModel,
    escuelas: List<Pair<String, String>>,
    asesores: List<Pair<String, String>>,
    loading: Boolean,
    snackbar: SnackbarHostState,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Estado del formulario
    var escuelaId by rememberSaveable { mutableStateOf<String?>(null) }
    var escuelaNombre by rememberSaveable { mutableStateOf("") }

    var asesoresSeleccionados by rememberSaveable { mutableStateOf(setOf<String>()) }

    var fechaIso by rememberSaveable { mutableStateOf("") }      // yyyy-MM-dd
    var horaEntrada by rememberSaveable { mutableStateOf("") }   // HH:mm
    var horaSalida by rememberSaveable { mutableStateOf("") }    // HH:mm

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // loading overlay
        if (loading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Cargando…")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Nueva visita", style = MaterialTheme.typography.headlineSmall)
            }

            item {
                EscuelasDropdown(
                    label = "Escuela",
                    opciones = escuelas,
                    seleccionadoId = escuelaId,
                    seleccionadoNombre = escuelaNombre,
                    onSeleccion = { id, nombre ->
                        escuelaId = id
                        escuelaNombre = nombre
                    }
                )
            }

            item {
                AsesoresDropdownMulti(
                    label = "Asesores",
                    opciones = asesores,
                    seleccionados = asesoresSeleccionados,
                    onSeleccionadosChange = { set -> asesoresSeleccionados = set }
                )
            }

            item {
                DatePickerFieldNative(
                    label = "Fecha",
                    value = fechaIso,
                    onValueChange = { fechaIso = it }
                )
            }

            item {
                TimePickerFieldNative(
                    label = "Hora de entrada",
                    value = horaEntrada,
                    onValueChange = { horaEntrada = it }
                )
            }

            item {
                TimePickerFieldNative(
                    label = "Hora de salida",
                    value = horaSalida,
                    onValueChange = { horaSalida = it }
                )
            }

            item {
                Button(
                    onClick = {
                        val ok = escuelaId != null &&
                                fechaIso.isNotBlank() &&
                                horaEntrada.isNotBlank() &&
                                horaSalida.isNotBlank() &&
                                asesoresSeleccionados.isNotEmpty()

                        if (!ok) {
                            scope.launch { snackbar.showSnackbar("Completa todos los campos.") }
                            return@Button
                        }

                        val asesoresNombres = asesores
                            .filter { asesoresSeleccionados.contains(it.first) }
                            .map { it.second }

                        val visita = Visita(
                            escuelaId = escuelaId!!,
                            fechaIso = fechaIso,
                            horaEntrada = horaEntrada,
                            horaSalida = horaSalida,
                            asesoresIds = asesoresSeleccionados.toList()
                        )

                        // Guardar (pasamos nombres denormalizados)
                        viewModel.crearVisita(
                            v = visita,
                            escuelaNombre = escuelaNombre,
                            asesoresNombres = asesoresNombres
                        ) { exito ->
                            scope.launch {
                                if (exito) {
                                    // 🔄 1) LIMPIA
                                    escuelaNombre = ""
                                    asesoresSeleccionados = emptySet()
                                    fechaIso = ""
                                    horaEntrada = ""
                                    horaSalida = ""
                                    onSaved()

                                    scope.launch { snackbar.showSnackbar("Visita guardada.") }
                                } else {
                                    snackbar.showSnackbar("No se pudo guardar.")
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar visita") }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ---------- Dropdown Escuelas (Material3 ExposedDropdown) ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscuelasDropdown(
    label: String,
    opciones: List<Pair<String, String>>,
    seleccionadoId: String?,
    seleccionadoNombre: String,
    onSeleccion: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val display = if (seleccionadoId == null) "" else seleccionadoNombre

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = { /* readOnly */ },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (opciones.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin datos") },
                    onClick = { expanded = false }
                )
            } else {
                opciones.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = { Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = {
                            onSeleccion(id, nombre)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/* ---------- Asesores: Dropdown multi‑select con checkboxes ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsesoresDropdownMulti(
    label: String,
    opciones: List<Pair<String, String>>,   // (id, nombre)
    seleccionados: Set<String>,
    onSeleccionadosChange: (Set<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val resumen = remember(opciones, seleccionados) {
        if (seleccionados.isEmpty()) ""
        else opciones.filter { seleccionados.contains(it.first) }
            .joinToString { it.second }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = resumen,
            onValueChange = { /* readOnly */ },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (opciones.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin datos") },
                    onClick = { expanded = false }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Limpiar selección") },
                    onClick = { onSeleccionadosChange(emptySet()) }
                )
                opciones.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = seleccionados.contains(id),
                                    onCheckedChange = null // toggle en onClick
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        },
                        onClick = {
                            val next = seleccionados.toMutableSet()
                            if (next.contains(id)) next.remove(id) else next.add(id)
                            onSeleccionadosChange(next)
                            // no cerramos para permitir seleccionar varios;
                            // el usuario puede tocar fuera o presionar "Hecho"
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Hecho") },
                    onClick = { expanded = false }
                )
            }
        }
    }
}

/* =========================
   HISTORIAL DE VISITAS
   ========================= */

@Composable
private fun HistorialVisitas(
    visitas: List<VisitasViewModel.VisitaUI>
) {
    if (visitas.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin visitas registradas")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visitas, key = { it.id }) { v ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(v.escuelaNombre, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Fecha: ${v.fechaIso}")
                    if (v.asesoresResumen.isNotBlank() && v.asesoresResumen != "—") {
                        Text("Asesores: ${v.asesoresResumen}")
                    }
                }
            }
        }
    }
}

/* ---------- Date/Time nativos (NO experimentales) ---------- */

@Composable
private fun DatePickerFieldNative(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val ctx = LocalContext.current
    val cal = Calendar.getInstance()

    if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        try {
            val (y, m, d) = value.split("-").map { it.toInt() }
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m - 1)
            cal.set(Calendar.DAY_OF_MONTH, d)
        } catch (_: Exception) { /* ignore */ }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH)
                val d = cal.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(ctx, { _, yy, mm, dd ->
                    val mm2 = (mm + 1).toString().padStart(2, '0')
                    val dd2 = dd.toString().padStart(2, '0')
                    onValueChange("$yy-$mm2-$dd2")
                }, y, m, d).show()
            }) { Icon(Icons.Filled.DateRange, contentDescription = null) }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TimePickerFieldNative(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val ctx = LocalContext.current
    val cal = Calendar.getInstance()

    if (value.matches(Regex("\\d{2}:\\d{2}"))) {
        try {
            val (h, m) = value.split(":").map { it.toInt() }
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, m)
        } catch (_: Exception) { /* ignore */ }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                val h = cal.get(Calendar.HOUR_OF_DAY)
                val m = cal.get(Calendar.MINUTE)
                TimePickerDialog(ctx, { _, hh, mm ->
                    val hh2 = hh.toString().padStart(2, '0')
                    val mm2 = mm.toString().padStart(2, '0')
                    onValueChange("$hh2:$mm2")
                }, h, m, true).show()
            }) { Icon(Icons.Filled.Schedule, contentDescription = null) }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
