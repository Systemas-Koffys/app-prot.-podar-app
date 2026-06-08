package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiArboristClient {
    private const val TAG = "GeminiArboristClient"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getForestryAssessment(
        especie: String,
        barrio: String,
        distrito: String,
        detalles: String,
        urgenciaSolicitada: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Clave de API de Gemini no configurada. Por favor, registre la clave de API de Gemini en el panel de secretos de AI Studio para activar la evaluación interactiva automática de especies."
        }

        val prompt = """
            Actúa como el Ingeniero Forestal de la Dirección de Ornato Público de la Alcaldía de Tarija.
            Realiza una evaluación técnica profesional detallada para la siguiente solicitud de poda/derribo urbano:
            
            - Especie: $especie
            - Ubicación: Barrio $barrio, Distrito $distrito (Tarija, Bolivia)
            - Descripción del problema: $detalles
            - Urgencia solicitada por el vecino: $urgenciaSolicitada
            
            Redacta un veredicto técnico e informe corto (máximo 250 palabras) estructurado de la siguiente manera:
            📌 **1. Evaluación de Riesgo y Viabilidad**: Analiza si la especie es apta y si realmente se justifica la poda o el derribo.
            🚨 **2. Nivel de Urgencia Recomendado**: Confirma o corrige el nivel de urgencia de acuerdo al peligro estructural o de infraestructura.
            🛠️ **3. Equipamiento Recomendado**: Indica qué herramientas son indispensables (ej. camión con canasta/plataforma hidráulica, marcas de motosierra, arneses de seguridad, etc.).
            🌱 **4. Compensación e Impacto**: Indica si se requiere autorización forestal municipal y cuántos plantines de especies nativas (ej. jarka, jacarandá, lapacho) deben replantarse en Tarija de ser un derribo.
            
            Mantén un tono formal, técnico y empático con la ecología urbana tarijeña.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed with code ${response.code}: $errorBody")
                    return@withContext "Error del servidor de respuesta Gemini (${response.code}). Asegúrate de que la clave de API sea válida."
                }
                
                val responseString = response.body?.string() ?: return@withContext "Respuesta vacía del servidor."
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "Sin texto.")
                    }
                }
                return@withContext "No se obtuvo una respuesta válida de evaluación."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Gemini API", e)
            return@withContext "No se pudo conectar al Asistente IA de Ornato Público. Detalle del error: ${e.localizedMessage}"
        }
    }
}
