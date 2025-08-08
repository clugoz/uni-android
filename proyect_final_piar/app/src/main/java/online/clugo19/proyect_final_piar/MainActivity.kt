package online.clugo19.proyect_final_piar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import online.clugo19.proyect_final_piar.navigation.AppNavHost
import online.clugo19.proyect_final_piar.ui.theme.Proyect_final_piarTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Proyect_final_piarTheme {
                AppNavHost(viewModel = viewModel)
            }

        }
    }
}

