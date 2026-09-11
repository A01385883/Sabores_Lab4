package mx.tec.sabores.ui.state

import retrofit2.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.RestaurantEnLista
import mx.tec.sabores.domain.Review
import java.io.IOException

data class MyReviewItem(val restaurantName: String, val review: Review)

/** El restaurante y sus reseñas, que la pantalla de detalle necesita juntos. */
data class Detalle(
    val restaurant: Restaurant,
    val reviews: List<Review>
) {

    val summary: RatingSummary = RatingSummary.from(reviews)
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class SaboresViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    var restaurantes by mutableStateOf<UiState<List<RestaurantEnLista>>>(UiState.Cargando)
        private set

    var detalle by mutableStateOf<UiState<Detalle>>(UiState.Cargando)
        private set

    var aviso by mutableStateOf<String?>(null)
        private set

    var mias by mutableStateOf<UiState<List<MyReviewItem>>>(UiState.Cargando)
        private set

    init { cargarRestaurantes() }

    fun cargarRestaurantes() {
        viewModelScope.launch {
            restaurantes = UiState.Cargando
            restaurantes = pedir { repository.getAllForList() }
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            detalle = UiState.Cargando
            detalle = pedir { Detalle(repository.getById(id), repository.getReviews(id)) }
        }
    }

    fun cargarReviews() {
        viewModelScope.launch {
            mias = UiState.Cargando
            mias = pedir {
                val nombres = repository.getAllForList().associate {
                    it.restaurant.id to it.restaurant.name
                }
                repository.getMyReviews().map { review ->
                    MyReviewItem(
                        restaurantName = nombres[review.restaurantId]
                            ?: "Restaurante ${review.restaurantId}",
                        review = review
                    )
                }
            }
        }
    }


    //Funciones de modificacion de reviews
    fun editarEstrellas(review: Review, stars: Int) {
        viewModelScope.launch {
            try {
                repository.editReview(review.id, stars = stars)
                cargarReviews()
            } catch (e: IOException) {
                aviso = "No hay conexion. No se pudo editar."
            } catch (e: HttpException) {
                aviso = mensajeDe(e)
            }
        }
    }

    fun borrar(review: Review) {
        viewModelScope.launch {
            try {
                val borrada = repository.deleteReview(review.id)
                if (!borrada) aviso = "Esa resena es de alguien mas."
                cargarReviews()
            } catch (e: IOException) {
                aviso = "No hay conexion. No se pudo borrar."
            } catch (e: HttpException) {
                aviso = mensajeDe(e)
            }
        }
    }

    fun limpiarAviso() {
        aviso = null
    }
    private suspend fun <T> pedir(block: suspend () -> T): UiState<T> = try {
        UiState.Exito(block())
    } catch (e: IOException) {
        UiState.Error("No hay conexión. Revisa tu internet.")
    } catch (e: HttpException) {
        UiState.Error(mensajeDe(e))
    }
}