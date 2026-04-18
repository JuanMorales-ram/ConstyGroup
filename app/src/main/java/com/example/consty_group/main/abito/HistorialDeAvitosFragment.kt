package com.example.consty_group.main.abito

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import java.util.Calendar

class HistorialDeAvitosFragment : Fragment(R.layout.fragment_historial_de_avitos) {

    private lateinit var datosAnuales: List<DiaAbito>
    private val diasSemana = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
    private val mesesNombres = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )

    private var tabSeleccionado = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        datosAnuales = HistorialMockData.generarAnioCompleto(anioActual)

        actualizarResumen(view)
        configurarTabs(view)
        construirVistaSemanal(view)
        construirVistaMensual(view)
        construirVistaAnual(view)
    }

    // ─── Resumen (3 cajones) ───────────────────────────────────────

    private fun actualizarResumen(view: View) {
        val datosFiltered = when (tabSeleccionado) {
            0 -> obtenerDatosSemanaActual()
            1 -> obtenerDatosMesActual()
            else -> datosAnuales
        }

        val fallidos = datosFiltered.count { it.estado == EstadoAbito.FALLIDO }
        val parciales = datosFiltered.count { it.estado == EstadoAbito.PARCIAL }
        val perfectos = datosFiltered.count { it.estado == EstadoAbito.PERFECTO }

        view.findViewById<TextView>(R.id.tvFallidoCount).text = fallidos.toString()
        view.findViewById<TextView>(R.id.tvParcialCount).text = parciales.toString()
        view.findViewById<TextView>(R.id.tvPerfectoCount).text = perfectos.toString()
    }

    // ─── Tabs ──────────────────────────────────────────────────────

    private fun configurarTabs(view: View) {
        val tabSemana = view.findViewById<TextView>(R.id.tabSemana)
        val tabMes = view.findViewById<TextView>(R.id.tabMes)
        val tabAnio = view.findViewById<TextView>(R.id.tabAnio)
        val tabs = listOf(tabSemana, tabMes, tabAnio)

        fun seleccionarTab(index: Int) {
            tabSeleccionado = index
            tabs.forEachIndexed { i, tab ->
                tab.setBackgroundResource(
                    if (i == index) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected
                )
            }
            actualizarResumen(view)
        }

        tabSemana.setOnClickListener { seleccionarTab(0) }
        tabMes.setOnClickListener { seleccionarTab(1) }
        tabAnio.setOnClickListener { seleccionarTab(2) }
    }

    // ─── Datos de semana / mes ─────────────────────────────────────

    private fun obtenerDatosSemanaActual(): List<DiaAbito> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val resultado = mutableListOf<DiaAbito>()
        for (i in 0..6) {
            val dia = cal.get(Calendar.DAY_OF_MONTH)
            val mes = cal.get(Calendar.MONTH)
            val anio = cal.get(Calendar.YEAR)
            datosAnuales.find { it.dia == dia && it.mes == mes && it.anio == anio }
                ?.let { resultado.add(it) }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return resultado
    }

    private fun obtenerDatosMesActual(): List<DiaAbito> {
        val cal = Calendar.getInstance()
        return datosAnuales.filter {
            it.mes == cal.get(Calendar.MONTH) && it.anio == cal.get(Calendar.YEAR)
        }
    }

    // ─── Vista Semanal (barras) ────────────────────────────────────

    private fun construirVistaSemanal(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.barChartContainer)
        val tvRango = view.findViewById<TextView>(R.id.tvRangoSemana)

        // Rango de la semana
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val inicioSemana = cal.get(Calendar.DAY_OF_MONTH)
        val mesInicio = cal.get(Calendar.MONTH)
        cal.add(Calendar.DAY_OF_MONTH, 6)
        val finSemana = cal.get(Calendar.DAY_OF_MONTH)
        val mesFin = cal.get(Calendar.MONTH)

        tvRango.text = "$inicioSemana - $finSemana ${mesesNombres[mesFin].lowercase()}"

        val datosSemana = obtenerDatosSemanaActual()
        val maxTareas = (datosSemana.maxOfOrNull { it.totalTareas } ?: 1).coerceAtLeast(1)
        val maxBarHeight = dpToPx(120)

        container.removeAllViews()

        for (i in 0..6) {
            val columnLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
            }

            if (i < datosSemana.size) {
                val dia = datosSemana[i]
                val barHeight = ((dia.totalTareas.toFloat() / maxTareas) * maxBarHeight).toInt()
                    .coerceAtLeast(dpToPx(4).toInt())
                val color = obtenerColor(dia.estado)

                val bar = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        dpToPx(28).toInt(), barHeight
                    ).apply {
                        bottomMargin = dpToPx(4).toInt()
                    }
                    background = crearBarDrawable(color)
                }
                columnLayout.addView(bar)
            }

            val label = TextView(requireContext()).apply {
                text = diasSemana[i]
                setTextColor(ContextCompat.getColor(requireContext(), R.color.TextoGris))
                textSize = 11f
                gravity = Gravity.CENTER
            }
            columnLayout.addView(label)

            container.addView(columnLayout)
        }
    }

    // ─── Vista Mensual (calendario) ────────────────────────────────

    private fun construirVistaMensual(view: View) {
        val grid = view.findViewById<GridLayout>(R.id.gridMensual)

        val cal = Calendar.getInstance()
        val mesActual = cal.get(Calendar.MONTH)
        val anioActual = cal.get(Calendar.YEAR)
        val hoy = cal.get(Calendar.DAY_OF_MONTH)

        val datosMes = obtenerDatosMesActual()

        cal.set(anioActual, mesActual, 1)
        val primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK)
        val offset = if (primerDiaSemana == Calendar.SUNDAY) 6 else primerDiaSemana - Calendar.MONDAY
        val diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        grid.removeAllViews()
        grid.columnCount = 7

        val cellSize = dpToPx(36).toInt()
        val margin = dpToPx(3).toInt()

        // Espacios vacíos antes del primer día
        for (i in 0 until offset) {
            grid.addView(View(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize; height = cellSize
                    setMargins(margin, margin, margin, margin)
                }
            })
        }

        // Días del mes
        for (dia in 1..diasEnMes) {
            val dato = datosMes.find { it.dia == dia }
            val isFuture = dia > hoy && mesActual == Calendar.getInstance().get(Calendar.MONTH)

            val color = when {
                isFuture -> Color.TRANSPARENT
                dato != null -> obtenerColor(dato.estado)
                else -> ContextCompat.getColor(requireContext(), R.color.historial_dia_vacio)
            }

            val tv = TextView(requireContext()).apply {
                text = dia.toString()
                gravity = Gravity.CENTER
                setTextColor(if (isFuture) Color.parseColor("#55FFFFFF") else Color.WHITE)
                textSize = 12f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize; height = cellSize
                    setMargins(margin, margin, margin, margin)
                }
                background = crearCirculoDrawable(color)
            }
            grid.addView(tv)
        }
    }

    // ─── Vista Anual (12 mini-meses) ──────────────────────────────

    private fun construirVistaAnual(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.containerAnual)
        container.removeAllViews()

        val anioActual = Calendar.getInstance().get(Calendar.YEAR)

        // 4 filas × 3 columnas = 12 meses
        for (fila in 0..3) {
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(16).toInt() }
            }

            for (col in 0..2) {
                val mes = fila * 3 + col

                val mesLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    ).apply { marginEnd = if (col < 2) dpToPx(8).toInt() else 0 }
                }

                // Nombre del mes
                val tvMes = TextView(requireContext()).apply {
                    text = mesesNombres[mes]
                    setTextColor(Color.WHITE)
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, dpToPx(4).toInt())
                }
                mesLayout.addView(tvMes)

                // Mini grilla del mes
                val datosMes = datosAnuales.filter { it.mes == mes }
                val calMes = Calendar.getInstance()
                calMes.set(anioActual, mes, 1)
                val diasEnMes = calMes.getActualMaximum(Calendar.DAY_OF_MONTH)
                val primerDia = calMes.get(Calendar.DAY_OF_WEEK)
                val offset = if (primerDia == Calendar.SUNDAY) 6 else primerDia - Calendar.MONDAY

                val miniGrid = GridLayout(requireContext()).apply {
                    columnCount = 7
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val dotSize = dpToPx(6).toInt()
                val dotMargin = dpToPx(1).toInt()

                for (i in 0 until offset) {
                    miniGrid.addView(View(requireContext()).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = dotSize; height = dotSize
                            setMargins(dotMargin, dotMargin, dotMargin, dotMargin)
                        }
                    })
                }

                for (dia in 1..diasEnMes) {
                    val dato = datosMes.find { it.dia == dia }
                    val color = if (dato != null) obtenerColor(dato.estado)
                    else ContextCompat.getColor(requireContext(), R.color.historial_dia_vacio)

                    val dot = View(requireContext()).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = dotSize; height = dotSize
                            setMargins(dotMargin, dotMargin, dotMargin, dotMargin)
                        }
                        background = crearCirculoDrawable(color)
                    }
                    miniGrid.addView(dot)
                }

                mesLayout.addView(miniGrid)
                rowLayout.addView(mesLayout)
            }

            container.addView(rowLayout)
        }

        // Leyenda anual
        val leyendaAnual = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8).toInt() }
        }

        val leyendas = listOf(
            Pair(R.color.historial_rojo, "0-49%"),
            Pair(R.color.historial_amarillo, "50-99%"),
            Pair(R.color.historial_verde, "100%")
        )
        for ((colorRes, texto) in leyendas) {
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(10).toInt(), dpToPx(10).toInt()).apply {
                    marginEnd = dpToPx(4).toInt()
                }
                background = crearCirculoDrawable(ContextCompat.getColor(requireContext(), colorRes))
            }
            leyendaAnual.addView(dot)

            val tv = TextView(requireContext()).apply {
                text = texto
                setTextColor(ContextCompat.getColor(requireContext(), R.color.TextoGris))
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dpToPx(16).toInt() }
            }
            leyendaAnual.addView(tv)
        }
        container.addView(leyendaAnual)
    }

    // ─── Utilidades ────────────────────────────────────────────────

    private fun obtenerColor(estado: EstadoAbito): Int {
        return when (estado) {
            EstadoAbito.FALLIDO -> ContextCompat.getColor(requireContext(), R.color.historial_rojo)
            EstadoAbito.PARCIAL -> ContextCompat.getColor(requireContext(), R.color.historial_amarillo)
            EstadoAbito.PERFECTO -> ContextCompat.getColor(requireContext(), R.color.historial_verde)
        }
    }

    private fun crearBarDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                dpToPx(6), dpToPx(6),
                dpToPx(6), dpToPx(6),
                0f, 0f,
                0f, 0f
            )
        }
    }

    private fun crearCirculoDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun dpToPx(dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
    }
}