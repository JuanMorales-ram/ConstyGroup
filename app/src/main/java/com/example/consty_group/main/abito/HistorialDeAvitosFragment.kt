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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.consty_group.R
import com.example.consty_group.data.HabitoRepository
import com.example.consty_group.data.EstadoAbito
import com.example.consty_group.data.DiaAbito
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistorialDeAvitosFragment : Fragment(R.layout.fragment_historial_de_avitos) {

    private var datosAnuales: MutableList<DiaAbito> = mutableListOf()
    private val diasSemana = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
    private val mesesNombres = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )

    private var tabSeleccionado = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos reales desde Supabase
        cargarDatosDesdeSupabase(view)
        configurarTabs(view)

        // Solución al error del botón volver (usando el ID btnVolver de tu XML)
        view.findViewById<View>(R.id.btnVolver)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun cargarDatosDesdeSupabase(view: View) {
        lifecycleScope.launch {
            try {
                val totalHabitosUsuario = HabitoRepository.obtenerHabitos().size
                val historialReal = HabitoRepository.obtenerHistorialCompleto()

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                datosAnuales.clear()

                historialReal.forEach { (fechaStr, habitosCompletados) ->
                    val date = sdf.parse(fechaStr) ?: return@forEach
                    val cal = Calendar.getInstance().apply { time = date }
                    val completados = habitosCompletados.size

                    val estado = when {
                        completados == 0 -> EstadoAbito.FALLIDO
                        completados < totalHabitosUsuario -> EstadoAbito.PARCIAL
                        else -> EstadoAbito.PERFECTO
                    }

                    datosAnuales.add(DiaAbito(
                        dia = cal.get(Calendar.DAY_OF_MONTH),
                        mes = cal.get(Calendar.MONTH),
                        anio = cal.get(Calendar.YEAR),
                        totalTareas = completados,
                        estado = estado
                    ))
                }

                actualizarResumen(view)
                construirVistaSemanal(view)
                construirVistaMensual(view)
                construirVistaAnual(view)

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al sincronizar historial", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                ?.let { resultado.add(it) } ?: resultado.add(DiaAbito(dia, mes, anio, 0, EstadoAbito.FALLIDO))
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

    private fun construirVistaSemanal(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.barChartContainer) ?: return
        val tvRango = view.findViewById<TextView>(R.id.tvRangoSemana) ?: return
        val cal = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY; set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val inicioSemana = cal.get(Calendar.DAY_OF_MONTH)
        val mesFin = cal.apply { add(Calendar.DAY_OF_MONTH, 6) }.get(Calendar.MONTH)
        val finSemana = cal.get(Calendar.DAY_OF_MONTH)

        tvRango.text = "$inicioSemana - $finSemana ${mesesNombres[mesFin].lowercase()}"

        val datosSemana = obtenerDatosSemanaActual()
        val maxTareas = (datosSemana.maxOfOrNull { it.totalTareas } ?: 1).coerceAtLeast(1)
        val maxBarHeight = dpToPx(120)

        container.removeAllViews()

        for (i in 0..6) {
            val columnLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }
            val dia = datosSemana[i]
            val barHeight = ((dia.totalTareas.toFloat() / maxTareas) * maxBarHeight).toInt().coerceAtLeast(dpToPx(4).toInt())
            val bar = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(28).toInt(), barHeight).apply { bottomMargin = dpToPx(4).toInt() }
                background = crearBarDrawable(obtenerColor(dia.estado))
            }
            columnLayout.addView(bar)
            val label = TextView(requireContext()).apply {
                text = diasSemana[i]; setTextColor(ContextCompat.getColor(requireContext(), R.color.TextoGris)); textSize = 11f; gravity = Gravity.CENTER
            }
            columnLayout.addView(label)
            container.addView(columnLayout)
        }
    }

    private fun construirVistaMensual(view: View) {
        val grid = view.findViewById<GridLayout>(R.id.gridMensual) ?: return
        val cal = Calendar.getInstance()
        val mesActual = cal.get(Calendar.MONTH); val anioActual = cal.get(Calendar.YEAR); val hoy = cal.get(Calendar.DAY_OF_MONTH)
        val datosMes = obtenerDatosMesActual()

        cal.set(anioActual, mesActual, 1)
        val offset = if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 6 else cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
        val diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        grid.removeAllViews(); grid.columnCount = 7
        val cellSize = dpToPx(36).toInt(); val margin = dpToPx(3).toInt()

        for (i in 0 until offset) grid.addView(View(requireContext()).apply { layoutParams = GridLayout.LayoutParams().apply { width = cellSize; height = cellSize; setMargins(margin, margin, margin, margin) } })

        for (dia in 1..diasEnMes) {
            val dato = datosMes.find { it.dia == dia }
            val isFuture = dia > hoy
            val color = if (isFuture) Color.TRANSPARENT else if (dato != null) obtenerColor(dato.estado) else ContextCompat.getColor(requireContext(), R.color.historial_dia_vacio)

            grid.addView(TextView(requireContext()).apply {
                text = dia.toString(); gravity = Gravity.CENTER; setTextColor(if (isFuture) Color.parseColor("#55FFFFFF") else Color.WHITE); textSize = 12f
                layoutParams = GridLayout.LayoutParams().apply { width = cellSize; height = cellSize; setMargins(margin, margin, margin, margin) }
                background = crearCirculoDrawable(color)
            })
        }
    }

    private fun construirVistaAnual(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.containerAnual) ?: return
        container.removeAllViews()
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)

        for (fila in 0..3) {
            val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dpToPx(16).toInt() } }
            for (col in 0..2) {
                val mes = fila * 3 + col
                val mesLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = if (col < 2) dpToPx(8).toInt() else 0 } }
                mesLayout.addView(TextView(requireContext()).apply { text = mesesNombres[mes]; setTextColor(Color.WHITE); textSize = 11f; gravity = Gravity.CENTER; setPadding(0, 0, 0, dpToPx(4).toInt()) })

                val miniGrid = GridLayout(requireContext()).apply { columnCount = 7; layoutParams = LinearLayout.LayoutParams(-1, -2) }
                val calMes = Calendar.getInstance().apply { set(anioActual, mes, 1) }
                val offset = if (calMes.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 6 else calMes.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
                val dotSize = dpToPx(6).toInt(); val dotMargin = dpToPx(1).toInt()

                for (i in 0 until offset) miniGrid.addView(View(requireContext()).apply { layoutParams = GridLayout.LayoutParams().apply { width = dotSize; height = dotSize; setMargins(dotMargin, dotMargin, dotMargin, dotMargin) } })
                for (dia in 1..calMes.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    val dato = datosAnuales.find { it.dia == dia && it.mes == mes }
                    val color = if (dato != null) obtenerColor(dato.estado) else ContextCompat.getColor(requireContext(), R.color.historial_dia_vacio)
                    miniGrid.addView(View(requireContext()).apply { layoutParams = GridLayout.LayoutParams().apply { width = dotSize; height = dotSize; setMargins(dotMargin, dotMargin, dotMargin, dotMargin) }; background = crearCirculoDrawable(color) })
                }
                mesLayout.addView(miniGrid); row.addView(mesLayout)
            }
            container.addView(row)
        }
    }

    private fun obtenerColor(estado: EstadoAbito): Int = when (estado) {
        EstadoAbito.FALLIDO -> ContextCompat.getColor(requireContext(), R.color.historial_rojo)
        EstadoAbito.PARCIAL -> ContextCompat.getColor(requireContext(), R.color.historial_amarillo)
        EstadoAbito.PERFECTO -> ContextCompat.getColor(requireContext(), R.color.historial_verde)
    }

    private fun crearBarDrawable(color: Int) = GradientDrawable().apply {
        setColor(color)
        val radius = dpToPx(6)
        cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
    }

    private fun crearCirculoDrawable(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun dpToPx(dp: Int): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
}