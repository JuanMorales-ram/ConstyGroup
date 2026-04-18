package com.example.consty_group.main.abito

import java.util.Calendar
import java.util.Random

data class DiaAbito(
    val dia: Int,
    val mes: Int,
    val anio: Int,
    val tareasCompletadas: Int,
    val totalTareas: Int
) {
    val porcentaje: Float
        get() = if (totalTareas == 0) 0f else (tareasCompletadas.toFloat() / totalTareas) * 100f

    val estado: EstadoAbito
        get() = when {
            porcentaje >= 100f -> EstadoAbito.PERFECTO
            porcentaje >= 50f -> EstadoAbito.PARCIAL
            else -> EstadoAbito.FALLIDO
        }
}

enum class EstadoAbito {
    FALLIDO, PARCIAL, PERFECTO
}

object HistorialMockData {

    fun generarAnioCompleto(anio: Int): List<DiaAbito> {
        val datos = mutableListOf<DiaAbito>()
        val calendar = Calendar.getInstance()
        calendar.set(anio, Calendar.JANUARY, 1)

        val random = Random(42L)

        while (calendar.get(Calendar.YEAR) == anio) {
            val totalTareas = random.nextInt(5) + 3 // 3 a 7 tareas por día
            val completadas = random.nextInt(totalTareas + 1)

            datos.add(
                DiaAbito(
                    dia = calendar.get(Calendar.DAY_OF_MONTH),
                    mes = calendar.get(Calendar.MONTH),
                    anio = anio,
                    tareasCompletadas = completadas,
                    totalTareas = totalTareas
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return datos
    }
}
