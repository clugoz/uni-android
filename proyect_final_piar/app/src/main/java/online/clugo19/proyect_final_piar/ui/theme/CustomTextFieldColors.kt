package online.clugo19.proyect_final_piar.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun customTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AzulPrimario,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = AzulPrimario,
        unfocusedLabelColor = Color.Gray,
        cursorColor = AzulPrimario,
        errorBorderColor = AmarilloOscuro,
        errorCursorColor = AmarilloOscuro,
        errorLabelColor = AmarilloOscuro
    )
}