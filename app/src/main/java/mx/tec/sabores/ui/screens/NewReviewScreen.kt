package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.ReviewError
import mx.tec.sabores.domain.ReviewValidator
import mx.tec.sabores.ui.components.StarPicker
import mx.tec.sabores.ui.state.NewReviewUiState
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReviewScreen(
    restaurant: Restaurant,
    uiState: NewReviewUiState,
    onStarsChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    //La 1ra solucion no sirvio, siguiente idea
    // Bloqueo inmediato a nivel de UI, independiente de la recomposición del ViewModel.
    var yaEnviado by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Reseñar ${restaurant.name}") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Cómo estuvo?", style = MaterialTheme.typography.titleMedium)
            StarPicker(value = uiState.stars, onValueChange = onStarsChange)

            OutlinedTextField(
                value = uiState.comment,
                onValueChange = onCommentChange,
                label = { Text("Tu reseña") },
                minLines = 4,
                isError = uiState.commentError != null,
                supportingText = {
                    Column {
                        val error = uiState.commentError
                        if (error != null) Text(error.message())
                        else Text("Te quedan ${uiState.charactersLeft} caracteres")

                        val errorDelServidor = uiState.errorAlGuardar
                        if (errorDelServidor != null) {
                            Text(
                                text = errorDelServidor,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    //Blockea la parte UI de permitir 2 Inputs
                    yaEnviado = true
                    onSave()
                },
                enabled = uiState.canSave && !yaEnviado,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (uiState.guardando) "Publicando…" else "Publicar reseña") }
        }
    }
}

// Traducir el error del dominio a espanol es trabajo de la UI, no del dominio.
private fun ReviewError.message(): String = when (this) {
    ReviewError.NoStars         -> "Selecciona de 1 a 5 estrellas"
    ReviewError.CommentTooShort -> "Escribe al menos ${ReviewValidator.COMMENT_MIN} caracteres"
    ReviewError.CommentTooLong  -> "Máximo ${ReviewValidator.COMMENT_MAX} caracteres"
}
