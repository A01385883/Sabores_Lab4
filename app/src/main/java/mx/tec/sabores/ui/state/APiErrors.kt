package mx.tec.sabores.ui.state

import retrofit2.HttpException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun mensajeDe(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    val mensaje = cuerpo
        ?.let { runCatching { Json.parseToJsonElement(it) }.getOrNull() }
        ?.jsonObject?.get("error")?.jsonPrimitive?.contentOrNull

    return when (e.code()) {
        401 -> "Falta tu matrícula en Network.alumno."
        403 -> mensaje ?: "Esa reseña no es tuya."
        404 -> "Eso ya no existe. Actualiza la lista."
        422 -> mensaje ?: "Los datos no son válidos."
        else -> "El servidor respondió ${e.code()}."
    }
}