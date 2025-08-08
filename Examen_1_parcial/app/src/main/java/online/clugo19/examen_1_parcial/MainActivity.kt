package online.clugo19.examen_1_parcial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import online.clugo19.examen_1_parcial.ui.theme.Examen_1_parcialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Examen_1_parcialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppMain(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Clase Producto
data class Producto(val nombre: String, val precio: Double, val categoria: String)

// Lista de productos
val productos = listOf(
    Producto("Laptop", 999.99, "Electrónica"),
    Producto("Camisa", 19.99, "Ropa"),
    Producto("Cargador", 14.50, "Electrónica"),
    Producto("Zapatos", 45.99, "Ropa"),
    Producto("Libro", 9.99, "Otros")
)

@Composable
fun AppMain(modifier: Modifier = Modifier) {
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Electrónica", "Ropa", "Otros")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        // Menú desplegable
        Box {
            Button(onClick = { expanded = true }) {
                Text("Filtrar: $categoriaSeleccionada")
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria) },
                        onClick = {
                            categoriaSeleccionada = categoria
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar productos filtrados en Cards
        productos
            .filter { it.categoria == categoriaSeleccionada || categoriaSeleccionada == "Todos" }
            .forEach { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nombre: ${producto.nombre}")
                        Text("Precio: $${producto.precio}")
                        Text("Categoría: ${producto.categoria}")
                    }
                }
            }
    }
}

@Preview(showBackground = true)
@Composable
fun AppMainPreview() {
    Examen_1_parcialTheme {
        AppMain()
    }
}
