package online.clugo19.proyect_final_piar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, Icons.Default.Home, "Inicio"),
    BottomNavItem("visitas", Icons.Default.Place, "Visitas"),
    BottomNavItem("atenciones", Icons.AutoMirrored.Filled.List, "Atenciones")
)
