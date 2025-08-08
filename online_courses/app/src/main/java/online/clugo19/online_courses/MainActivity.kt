package online.clugo19.online_courses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import online.clugo19.online_courses.ui.theme.Online_coursesTheme

data class Course(
    val name: String,
    val hours: Int,
    val rating: Float
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Online_coursesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(title = { Text("Registro de Cursos") })
                    }
                ) { innerPadding ->
                    CourseScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CourseScreen(modifier: Modifier = Modifier) {
    // Estado de la lista de cursos
    var courseList by remember { mutableStateOf(listOf<Course>()) }

    // Estados de los campos de texto
    var name by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("") }
    var ratingText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Formulario de ingreso
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del curso") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = hoursText,
            onValueChange = { hoursText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Duración (horas)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ratingText,
            onValueChange = {
                // Permitimos dígitos y punto
                ratingText = it.filter { ch -> ch.isDigit() || ch == '.' }
            },
            label = { Text("Calificación (0.0 - 5.0)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                // Validación básica
                val hrs = hoursText.toIntOrNull() ?: 0
                val rt = ratingText.toFloatOrNull() ?: 0f
                if (name.isNotBlank() && hrs > 0 && rt in 0f..5f) {
                    val newCourse = Course(name.trim(), hrs, rt)
                    courseList = courseList + newCourse
                    // Limpiar campos
                    name = ""
                    hoursText = ""
                    ratingText = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Agregar curso")
        }

        Divider()

        // Lista de cursos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(courseList) { course ->
                CourseCard(course)
            }
        }
    }
}

@Composable
fun CourseCard(course: Course) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = course.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duración: ${course.hours} h",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Mostramos rating con estrellas
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Calificación:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.1f", course.rating),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseScreenPreview() {
    Online_coursesTheme {
        CourseScreen()
    }
}
