package online.clugo19.proyect_final_piar.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import online.clugo19.proyect_final_piar.navigation.Routes
import online.clugo19.proyect_final_piar.ui.theme.AzulPrimario

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = AzulPrimario,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = currentRoute == Routes.HOME,
            onClick = {
                if (currentRoute != Routes.HOME) {
                    navController.navigate(Routes.HOME)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = AzulPrimario
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.School, contentDescription = "Visitas") },
            label = { Text("Visitas") },
            selected = currentRoute == Routes.VISITAS,
            onClick = {
                if (currentRoute != Routes.VISITAS) {
                    navController.navigate(Routes.VISITAS)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = AzulPrimario
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Atenciones") },
            label = { Text("Atenciones") },
            selected = currentRoute == Routes.ATENCIONES,
            onClick = {
                if (currentRoute != Routes.ATENCIONES) {
                    navController.navigate(Routes.ATENCIONES)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = AzulPrimario
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = currentRoute == Routes.PERFIL,
            onClick = {
                if (currentRoute != Routes.PERFIL) {
                    navController.navigate(Routes.PERFIL)
                }},
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = AzulPrimario
            )
        )

    }
}
