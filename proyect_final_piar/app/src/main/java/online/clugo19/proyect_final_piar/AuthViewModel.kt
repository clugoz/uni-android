package online.clugo19.proyect_final_piar

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import online.clugo19.proyect_final_piar.ui.state.AuthUiState

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isEmpty() || password.isEmpty()) {
            uiState = uiState.copy(error = "Completa ambos campos.")
            return
        }

        uiState = uiState.copy(loading = true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                uiState = uiState.copy(loading = false)
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val message = when (val exception = task.exception) {
                        is FirebaseAuthInvalidUserException -> "El usuario no existe."
                        is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta."
                        else -> exception?.localizedMessage ?: "Error al iniciar sesión."
                    }
                    uiState = uiState.copy(error = message)
                }
            }
    }

    fun register(onSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isEmpty() || password.length < 6) {
            uiState = uiState.copy(error = "La contraseña debe tener al menos 6 caracteres.")
            return
        }

        uiState = uiState.copy(loading = true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                uiState = uiState.copy(loading = false)
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    uiState = uiState.copy(error = task.exception?.message)
                }
            }
    }

    fun resetPassword() {
        val email = uiState.email.trim()
        if (email.isEmpty()) {
            uiState = uiState.copy(error = "Escribe tu correo electrónico.")
            return
        }

        uiState = uiState.copy(loading = true, error = null)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                uiState = uiState.copy(loading = false)
                if (task.isSuccessful) {
                    uiState = uiState.copy(error = "Correo de recuperación enviado.")
                } else {
                    val message = when (val exception = task.exception) {
                        is FirebaseAuthInvalidUserException -> "El usuario no existe."
                        is FirebaseAuthInvalidCredentialsException -> "Correo inválido."
                        else -> exception?.localizedMessage ?: "Error al enviar correo."
                    }
                    uiState = uiState.copy(error = message)
                }
            }
    }


    fun clearError() {
        uiState = uiState.copy(error = null)
    }

}
