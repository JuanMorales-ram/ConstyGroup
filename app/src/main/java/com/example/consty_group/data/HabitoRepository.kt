package com.example.consty_group.data


import com.example.consty_group.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
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
                      var esComplejo: Boolean = false,
                      var fotoUrl: String? = null

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

    suspend fun toggleHabitoHoy(habitoId: String, completado: Boolean, fotoUrl: String? = null) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return
        val tabla = SupabaseClient.client.postgrest["registros_habitos"]
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val fechaHoy = sdf.format(java.util.Date())

        if (completado) {
            // Primero intentamos actualizar si ya existe
            val existentes = tabla.select {
                filter {
                    eq("habito_id", habitoId)
                    eq("usuario_id", userId)
                    eq("fecha", fechaHoy)
                }
            }.decodeList<Map<String, String?>>()

            if (existentes.isNotEmpty()) {
                // Ya existe → solo actualizamos foto_url si hay foto
                android.util.Log.d("FOTO_DEBUG", "Registro existe, actualizando foto_url: $fotoUrl")
                if (fotoUrl != null) {
                    tabla.update({
                        set("foto_url", fotoUrl)
                    }) {
                        filter {
                            eq("habito_id", habitoId)
                            eq("usuario_id", userId)
                            eq("fecha", fechaHoy)
                        }
                    }
                    android.util.Log.d("FOTO_DEBUG", "Update ejecutado")

                }
            } else {
                android.util.Log.d("FOTO_DEBUG", "Registro nuevo, insertando con foto_url: $fotoUrl")

                // No existe → insertar nuevo
                val urlFoto = fotoUrl
                val nuevoDoc = buildJsonObject {
                    put("habito_id", JsonPrimitive(habitoId))
                    put("usuario_id", JsonPrimitive(userId))
                    put("fecha", JsonPrimitive(fechaHoy))
                    if (urlFoto != null) put("foto_url", JsonPrimitive(urlFoto))
                }
                tabla.insert(nuevoDoc)
                android.util.Log.d("FOTO_DEBUG", "Insert ejecutado")

            }
        } else {
            // Desmarcar → borrar registro
            tabla.delete {
                filter {
                    eq("habito_id", habitoId)
                    eq("usuario_id", userId)
                    eq("fecha", fechaHoy)
                }
            }
        }
    }

    //LOGICA CARGA INICIAL DE DATOS: para que la lista no aparezca vacia al abrir la app
    //para saber si ya lo marcamos hoy
    suspend fun obtenerRegistrosHoy(): Map<String, String?> {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return emptyMap()
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
                .decodeList<Map<String, String?>>()  // ← String? no String

            resultado.associate { (it["habito_id"] ?: "") to it["foto_url"] }
        } catch (e: Exception) {
            emptyMap()
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

    //foto del habito
    suspend fun subirFotoHabito(bytes: ByteArray, fileName: String, habitoId: String): String {
        val storage = SupabaseClient.client.storage["fotos_habitos"]
        val path    = "$habitoId/$fileName"
        storage.upload(path, bytes) { upsert = true }
        return storage.publicUrl(path)
    }

    //logica para el  HISTORIAL DE RACHAS

    /** Obtiene los IDs de los días que un hábito fue completado (historial completo) */
    suspend fun obtenerHistorialDeHabito(habitoId: String): List<String> {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return emptyList()
        return try {
            val resultado = SupabaseClient.client.postgrest["registros_habitos"]
                .select {
                    filter {
                        eq("habito_id", habitoId)
                        eq("usuario_id", userId)
                    }
                }
                // Cambiamos String por Any? para que no falle con nulos o IDs
                .decodeList<Map<String, kotlinx.serialization.json.JsonElement>>()

            resultado.map { row ->
                // Extraemos la fecha limpiando las comillas si vienen del JsonElement
                row["fecha"]?.toString()?.replace("\"", "") ?: ""
            }.filter { it.isNotEmpty() }

        } catch (e: Exception) {
            android.util.Log.e("REPOSITORIO", "Error obteniendo historial: ${e.message}")
            emptyList()
        }
    }

    /** Lógica para calcular la racha actual */
    fun calcularRacha(fechas: List<String>): Int {
        if (fechas.isEmpty()) return 0

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val hoyMillis = sdf.parse(sdf.format(java.util.Date())).time
        val unDiaMillis = 24 * 60 * 60 * 1000L

        // Ordenar fechas de más reciente a más antigua
        val fechasMillis = fechas.mapNotNull {
            try { sdf.parse(it).time } catch(e: Exception) { null }
        }.distinct().sortedDescending()

        if (fechasMillis.isEmpty()) return 0

        var racha = 0
        var fechaEsperada = fechasMillis.first()

        // Verificamos si la fecha más reciente es hoy o ayer
        // Si la última vez que lo hizo fue antes de ayer, la racha es 0
        if (fechaEsperada < hoyMillis - unDiaMillis) {
            return 0
        }

        for (fecha in fechasMillis) {
            if (fecha == fechaEsperada) {
                racha++
                fechaEsperada -= unDiaMillis
            } else {
                break // Hueco detectado
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
            .decodeList<Map<String, String?>>()

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