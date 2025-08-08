package online.clugo19.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import online.clugo19.todo.ui.theme.ToDoTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            ToDoTheme {
                val viewModel: ToDoViewModel = viewModel()
                var showDialog by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Mis Tareas") })
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        if (viewModel.todos.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                            ) {
                                Text("No tienes tareas pendientes 😃")
                            }
                        } else {
                            LazyColumn {
                                items(viewModel.todos) { todo ->
                                    Card(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(todo.name, style = MaterialTheme.typography.titleLarge)
                                            Spacer(Modifier.height(4.dp))
                                            Text(todo.description, style = MaterialTheme.typography.bodyMedium)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Hora: ${todo.time}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showDialog) {
                        AddToDoDialog(
                            onDismiss = { showDialog = false },
                            onAdd = { todo ->
                                viewModel.addToDo(todo) { success ->
                                    showDialog = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
