package online.clugo19.todo
import com.google.firebase.firestore.FirebaseFirestore

class ToDoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val todosRef = db.collection("todos")

    fun listenToDos(onResult: (List<ToDo>) -> Unit) {
        todosRef.addSnapshotListener { snapshot, _ ->
            val todos = snapshot?.documents?.mapNotNull { it.toObject(ToDo::class.java) } ?: emptyList()
            onResult(todos)
        }
    }

    fun addToDo(todo: ToDo, onResult: (Boolean) -> Unit) {
        val doc = todosRef.document()
        doc.set(todo.copy(id = doc.id))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}