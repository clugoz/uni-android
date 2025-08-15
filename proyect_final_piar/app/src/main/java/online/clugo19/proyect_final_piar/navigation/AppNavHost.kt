package online.clugo19.proyect_final_piar.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import online.clugo19.proyect_final_piar.viewmodel.AuthViewModel
import online.clugo19.proyect_final_piar.viewmodel.VisitasViewModel
import online.clugo19.proyect_final_piar.components.BottomBar
import online.clugo19.proyect_final_piar.ui.screens.*

@Composable
fun AppNavHost(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val isLoggedIn = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val showBottomBar = isLoggedIn.value && currentDestination in listOf(
        Routes.HOME, Routes.VISITAS, Routes.ATENCIONES, Routes.PERFIL
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn.value) Routes.HOME else Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        isLoggedIn.value = true
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        isLoggedIn.value = false
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.VISITAS) {
                val visitasVM: VisitasViewModel = viewModel()
                VisitasScreen(
                    viewModel = visitasVM,
                    onSaved = {
                        // lo que quieras hacer después de guardar, por ejemplo volver:
                        // navController.popBackStack()
                    }
                )
            }

            composable(Routes.ATENCIONES) { AtencionesScreen() }

            composable(Routes.PERFIL) {
                PerfilScreen(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

        }
    }
}



