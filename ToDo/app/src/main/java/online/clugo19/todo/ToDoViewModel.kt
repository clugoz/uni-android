package online.clugo19.todo
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class ToDoViewModel : ViewModel() {
    private val repo = ToDoRepository()
    var todos by mutableStateOf(listOf<ToDo>())
        private set

    init {
        repo.listenToDos { todos = it }
    }

    fun addToDo(todo: ToDo, onResult: (Boolean) -> Unit) {
        repo.addToDo(todo, onResult)
    }
}