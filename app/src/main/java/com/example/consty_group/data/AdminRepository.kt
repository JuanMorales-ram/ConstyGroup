package com.example.consty_group.data

import com.example.consty_group.R
import com.example.consty_group.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

object AdminRepository {

    // ── Tarjeta: total de usuarios registrados ──────────────────────────────
    suspend fun contarTotalUsuarios(): Int {
        return try {
            SupabaseClient.client.postgrest["usuarios"]
                .select(Columns.raw("id"))
                .decodeList<Map<String, String>>()
                .size
        } catch (e: Exception) { 0 }
    }

    // ── Tarjeta: usuarios que registraron al menos 1 hábito hoy ─────────────
    suspend fun contarActivosHoy(): Int {
        val hoy = fechaHoy()
        return try {
            SupabaseClient.client.postgrest["registros_habitos"]
                .select(Columns.raw("usuario_id")) {
                    filter { eq("fecha", hoy) }
                }
                .decodeList<Map<String, String>>()
                .map { it["usuario_id"] }
                .distinct()
                .size
        } catch (e: Exception) { 0 }
    }

    // ── Tarjeta: usuarios activos en los últimos 7 días ──────────────────────
    suspend fun contarActivosSemana(): Int {
        val hace7 = fechaHaceNDias(7)
        return try {
            SupabaseClient.client.postgrest["registros_habitos"]
                .select(Columns.raw("usuario_id")) {
                    filter { gte("fecha", hace7) }
                }
                .decodeList<Map<String, String>>()
                .map { it["usuario_id"] }
                .distinct()
                .size
        } catch (e: Exception) { 0 }
    }

    // ── Tarjeta: usuarios activos en los últimos 30 días ─────────────────────
    suspend fun contarActivosMes(): Int {
        val hace30 = fechaHaceNDias(30)
        return try {
            SupabaseClient.client.postgrest["registros_habitos"]
                .select(Columns.raw("usuario_id")) {
                    filter { gte("fecha", hace30) }
                }
                .decodeList<Map<String, String>>()
                .map { it["usuario_id"] }
                .distinct()
                .size
        } catch (e: Exception) { 0 }
    }

    // ── Tasa de completación: hábitos completados hoy / total hábitos activos ─
    suspend fun calcularTasaCompletacion(): Int {
        return try {
            val totalHabitos = SupabaseClient.client.postgrest["habitos"]
                .select(Columns.raw("id"))
                .decodeList<Map<String, String>>()
                .size

            val completadosHoy = SupabaseClient.client.postgrest["registros_habitos"]
                .select(Columns.raw("id")) {
                    filter { eq("fecha", fechaHoy()) }
                }
                .decodeList<Map<String, String>>()
                .size

            if (totalHabitos == 0) 0
            else ((completadosHoy.toFloat() / totalHabitos) * 100).toInt().coerceAtMost(100)
        } catch (e: Exception) { 0 }
    }

    // ── Tendencia semanal: % de completación por día ─────────────────────────
    data class DatoGrafico(val label: String, val valor: Int)

    suspend fun obtenerTendenciaSemanal(): List<DatoGrafico> {
        val labels = listOf("L", "M", "X", "J", "V", "S", "D")
        return try {
            val totalHabitos = SupabaseClient.client.postgrest["habitos"]
                .select(Columns.raw("id"))
                .decodeList<Map<String, String>>()
                .size
                .takeIf { it > 0 }
                ?: return listOf(DatoGrafico("—", 0))

            (6 downTo 0).mapIndexed { i, diasAtras ->
                val fecha = fechaHaceNDias(diasAtras)
                val completados = SupabaseClient.client.postgrest["registros_habitos"]
                    .select(Columns.raw("id")) {
                        filter { eq("fecha", fecha) }
                    }
                    .decodeList<Map<String, String>>()
                    .size
                val pct = ((completados.toFloat() / totalHabitos) * 100).toInt().coerceAtMost(100)
                DatoGrafico(labels[i], pct)
            }
        } catch (e: Exception) { listOf(DatoGrafico("—", 0)) }
    }

    // ── Tendencia mensual: % por semana ──────────────────────────────────────
    suspend fun obtenerTendenciaMensual(): List<DatoGrafico> {
        return try {
            val totalHabitos = SupabaseClient.client.postgrest["habitos"]
                .select(Columns.raw("id"))
                .decodeList<Map<String, String>>()
                .size
                .takeIf { it > 0 }
                ?: return listOf(DatoGrafico("—", 0))

            (3 downTo 0).mapIndexed { i, semana ->
                val desde = fechaHaceNDias((semana + 1) * 7)
                val hasta = fechaHaceNDias(semana * 7)
                val completados = SupabaseClient.client.postgrest["registros_habitos"]
                    .select(Columns.raw("id")) {
                        filter {
                            gte("fecha", desde)
                            lte("fecha", hasta)
                        }
                    }
                    .decodeList<Map<String, String>>()
                    .size
                val pct = ((completados.toFloat() / totalHabitos) * 100).toInt().coerceAtMost(100)
                DatoGrafico("S${i + 1}", pct)
            }
        } catch (e: Exception) { listOf(DatoGrafico("—", 0)) }
    }

    // ── Hábitos más populares (top 5 con más registros) ──────────────────────
    data class HabitoStats(
        val nombre: String,
        val emoji: String,
        val usuarios: Int,
        val porcentaje: Int,
        val rank: String,
        val colorRes: Int
    )

    private val rankLabels = listOf("1", "2", "3", "4", "5")
    private val rankColors = listOf(
        R.color.colorMeta,
        R.color.progressEstaSemana,
        R.color.bajoAdmin,
        R.color.texto1Admin,
        R.color.texto1Admin
    )

    suspend fun obtenerHabitosPopulares(): List<HabitoStats> {
        return try {
            val totalUsuarios = contarTotalUsuarios().takeIf { it > 0 } ?: 1

            val registros = SupabaseClient.client.postgrest["registros_habitos"]
                .select(Columns.raw("habito_id, usuario_id")) {
                    filter { gte("fecha", fechaHaceNDias(30)) }
                }
                .decodeList<Map<String, String>>()

            val conteo = registros
                .groupBy { it["habito_id"] ?: "" }
                .mapValues { (_, regs) -> regs.map { it["usuario_id"] }.distinct().size }

            val top5ids = conteo.entries.sortedByDescending { it.value }.take(5).map { it.key }

            val habitos = SupabaseClient.client.postgrest["habitos"]
                .select()
                .decodeList<Habito>()
                .associateBy { it.id ?: "" }

            top5ids.mapIndexedNotNull { index, id ->
                val habito   = habitos[id] ?: return@mapIndexedNotNull null
                val usuarios = conteo[id] ?: 0
                val pct      = ((usuarios.toFloat() / totalUsuarios) * 100).toInt().coerceAtMost(100)
                HabitoStats(
                    nombre     = habito.nombre,
                    emoji      = habito.emoji,
                    usuarios   = usuarios,
                    porcentaje = pct,
                    rank       = rankLabels.getOrElse(index) { "${index + 1}" },
                    colorRes   = rankColors.getOrElse(index) { R.color.texto1Admin }
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Helpers de fecha ─────────────────────────────────────────────────────
    fun fechaHoy(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun fechaHaceNDias(n: Int): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -n)
        return sdf.format(cal.time)
    }
}