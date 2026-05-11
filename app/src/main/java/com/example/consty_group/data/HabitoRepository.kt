package com.example.consty_group.data


import com.example.consty_group.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


@Serializable
data class Habito(    val id: String? = null,
                      val usuario_id: String? = null,
                      val nombre: String = "",
                      val emoji: String = "📌",
                      val icono_res_id: Int = 0,
                      val color_hex: String = "",
                      val recordatorio_activo: Boolean = false,
                      val hora_recordatorio: String = "",
                      val dias_semana: String = "",
                      var completadoHoy: Boolean = false,
                      var esComplejo: Boolean = false

)

enum class EstadoAbito { FALLIDO, PARCIAL, PERFECTO }
data class DiaAbito(
    val dia: Int,
    val mes: Int,
    val anio: Int,
    val totalTareas: Int,
    val estado: EstadoAbito
)


object HabitoRepository {
    private val postgrest = SupabaseClient.client.postgrest["habitos"]

    suspend fun crearHabito(habito: Habito) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return
        val nuevoHabito = habito.copy(usuario_id = userId)
        postgrest.insert(nuevoHabito)
    }

    suspend fun obtenerHabitos(): List<Habito> {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return emptyList()
        return postgrest.select {
            filter {
                eq("usuario_id", userId)
            }
        }.decodeList<Habito>()
    }

    //--logica del registro diario--------------------------------------------

    suspend fun toggleHabitoHoy(habitoId: String, completado: Boolean) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return
        val tabla = SupabaseClient.client.postgrest["registros_habitos"]

        // Forma compatible con API 24 para obtener la fecha actual (yyyy-MM-dd)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val fechaHoy = sdf.format(java.util.Date())

        if (completado) {
            // Insertar registro
            val nuevoDoc = buildJsonObject {
                put("habito_id", habitoId)
                put("usuario_id", userId)
                put("fecha", fechaHoy)
            }
            tabla.insert(nuevoDoc)
        } else {
            // Eliminar registro si el usuario se arrepiente
            tabla.delete {
                filter {
                    eq("habito_id", habitoId)
                    eq("usuario_id", userId)
                    eq("fecha", fechaHoy )
                }
            }
        }
    }

    //LOGICA CARGA INICIAL DE DATOS: para que la lista no aparezca vacia al abrir la app
    //para saber si ya lo marcamos hoy
    suspend fun obtenerRegistrosHoy(): List<String> {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return emptyList()

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val fechaHoy = sdf.format(java.util.Date())

        return try {
            val resultado = SupabaseClient.client.postgrest["registros_habitos"]
                .select {
                    filter {
                        eq("usuario_id", userId)
                        eq("fecha", fechaHoy)
                    }
                }
                .decodeList<Map<String, String>>()

            // Extraemos solo los IDs de los hábitos
            resultado.map { it["habito_id"] ?: "" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    //eliminar habito de supabase --------------------------
    suspend fun eliminarHabito(id: String) {
        postgrest.delete {
            filter {
                eq("id", id)
            }
        }
    }

    //logica para el  HISTORIAL DE RACHAS

    /** Obtiene los IDs de los días que un hábito fue completado (historial completo) */
    suspend fun obtenerHistorialDeHabito(habitoId: String): List<String> {
        return try {
            val resultado = SupabaseClient.client.postgrest["registros_habitos"]
                .select {
                    filter { eq("habito_id", habitoId) }
                }
                .decodeList<Map<String, String>>()

            resultado.map { it["fecha"] ?: "" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Lógica para calcular la racha actual */
    fun calcularRacha(fechas: List<String>): Int {
        if (fechas.isEmpty()) return 0

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val hoy = java.util.Calendar.getInstance()

        // Convertir strings a milisegundos y ordenar de más reciente a más antiguo
        val fechasMillis = fechas.map { sdf.parse(it).time }.sortedDescending()

        var racha = 0
        val unDiaMillis = 24 * 60 * 60 * 1000L

        // Empezamos comparando con "hoy"
        var fechaComparar = sdf.parse(sdf.format(hoy.time)).time

        for (fecha in fechasMillis) {
            if (fecha == fechaComparar) {
                racha++
                fechaComparar -= unDiaMillis // Restamos un día para la siguiente vuelta
            } else if (fecha < fechaComparar) {
                // Si hay un hueco en las fechas, la racha se rompe
                break
            }
        }
        return racha
    }


    //logica para el historial de habitos en el perfil
    /** Obtiene el historial completo de actividades del usuario */
    suspend fun obtenerHistorialCompleto(): List<Pair<String, List<Habito>>> {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return emptyList()

        // 1. Obtenemos todos los hábitos del usuario para tener sus nombres/colores
        val misHabitos = obtenerHabitos().associateBy { it.id }

        // 2. Obtenemos todos los registros de la tabla 'registros_habitos'
        val registros = SupabaseClient.client.postgrest["registros_habitos"]
            .select {
                filter { eq("usuario_id", userId) }
            }
            .decodeList<Map<String, String>>()

        // 3. Agrupamos por fecha y mapeamos a objetos Habito
        return registros
            .groupBy { it["fecha"] ?: "" }
            .map { (fecha, listaRegs) ->
                val habitosDeEsaFecha = listaRegs.mapNotNull { reg ->
                    misHabitos[reg["habito_id"]]
                }
                fecha to habitosDeEsaFecha
            }
            .sortedByDescending { it.first } // De más reciente a más antiguo
    }





}