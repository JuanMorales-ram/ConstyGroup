package com.example.consty_group.admin

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * EstadisticasFragment
 * ─────────────────────
 * Panel de Estadísticas y Reportes del admin.
 */
class EstadisticasFragment : Fragment() {

    private val totalUsuarios = 1284

    data class DatosBarra(val label: String, val valor: Int, val sesiones: Int)

    private val datosSemana = listOf(
        DatosBarra("L",  62, 795), DatosBarra("M",  58, 744), DatosBarra("X",  71, 911),
        DatosBarra("J",  67, 859), DatosBarra("V",  89, 1142), DatosBarra("S", 74, 949),
        DatosBarra("D",  55, 705)
    )
    private val datosMes = listOf(
        DatosBarra("S1", 65, 834), DatosBarra("S2", 70, 898),
        DatosBarra("S3", 75, 962), DatosBarra("S4", 88, 1129)
    )

    data class HabitoAbandono(val emoji: String, val nombre: String, val abandono: Int, val colorRes: Int)
    private val habitosAbandono = listOf(
        HabitoAbandono("🌙", "Dormir 8h",   46, R.color.bajoAdmin),
        HabitoAbandono("📖", "Leer 30 min", 38, R.color.medioAdmin),
        HabitoAbandono("🏋️", "Ejercicio",   31, R.color.amarilloAdmin),
        HabitoAbandono("🧘", "Meditación",  19, R.color.progressEstaSemana)
    )

    data class RangoProgreso(val rango: String, val cantidad: Int, val colorRes: Int)
    private val rangos = listOf(
        RangoProgreso("0–30%",   87,  R.color.bajoAdmin),
        RangoProgreso("31–60%",  298, R.color.medioAdmin),
        RangoProgreso("61–80%",  522, R.color.progressEstaSemana),
        RangoProgreso("81–100%", 377, R.color.envivoTextAdmin)
    )

    private val tendenciaMensual = listOf(58, 62, 66, 69, 71, 73)

    private var periodoActual = "mes"
    private var diaSeleccionado = 3 // S4 por defecto en mes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_estadisticas_reportes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarFecha(view)
        configurarTabs(view)
        configurarKPIs(view)
        configurarHabitosAbandono(view)
        configurarActividadDia(view)
        configurarProgresoRangos(view)
        configurarTendenciaMensual(view)
        configurarExportar(view)
    }

    private fun configurarFecha(view: View) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val mesStr = sdf.format(Date()).replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.tvFechaReporte).text = getString(R.string.datos_actualizados_format, mesStr)
    }

    private fun configurarTabs(view: View) {
        val tabSemana = view.findViewById<TextView>(R.id.tabSemana)
        val tabMes    = view.findViewById<TextView>(R.id.tabMes)
        val tabAnio   = view.findViewById<TextView>(R.id.tabAnio)

        fun seleccionar(tab: TextView, periodo: String) {
            val context = context ?: return
            listOf(tabSemana, tabMes, tabAnio).forEach {
                it.setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
                it.setBackgroundResource(0)
            }
            tab.setTextColor(ContextCompat.getColor(context, R.color.white))
            tab.setBackgroundResource(R.drawable.bg_tab_selected)
            periodoActual = periodo
            val datos = if (periodo == "semana") datosSemana else datosMes
            diaSeleccionado = datos.indexOfFirst { it.valor == datos.maxOf { d -> d.valor } }
            dibujarGraficoActividad(view, datos)
            actualizarInfoDia(view, datos)
        }

        tabSemana.setOnClickListener { seleccionar(tabSemana, "semana") }
        tabMes.setOnClickListener    { seleccionar(tabMes, "mes") }
        tabAnio.setOnClickListener   { seleccionar(tabAnio, "anio") }

        seleccionar(tabMes, "mes")
    }

    private fun configurarKPIs(view: View) {
        view.findViewById<TextView>(R.id.tvMasAbandono).text    = getString(R.string._46)
        view.findViewById<TextView>(R.id.tvDiaMasActivo).text   = getString(R.string.vie)
        view.findViewById<TextView>(R.id.tvPromCompletacion).text = getString(R.string._73)
    }

    private fun configurarHabitosAbandono(view: View) {
        val context = context ?: return
        val container = view.findViewById<LinearLayout>(R.id.llHabitosAbandono)
        container.removeAllViews()

        habitosAbandono.forEach { h ->
            val fila = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(10) }
            }

            val cabecera = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(5) }
            }

            val tvEmoji = TextView(context).apply {
                text     = h.emoji
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(dpToPx(24), LinearLayout.LayoutParams.WRAP_CONTENT)
                    .also { it.marginEnd = dpToPx(8) }
            }
            val tvNombre = TextView(context).apply {
                text     = h.nombre
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvPct = TextView(context).apply {
                text     = "${h.abandono}%"
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, h.colorRes))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            cabecera.addView(tvEmoji)
            cabecera.addView(tvNombre)
            cabecera.addView(tvPct)

            val track = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(3)
                )
                setBackgroundColor(ContextCompat.getColor(context, R.color.progressBarVacia))
            }
            val fill = View(context).apply {
                setBackgroundColor(ContextCompat.getColor(context, h.colorRes))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            }
            track.addView(fill)

            fila.addView(cabecera)
            fila.addView(track)
            container.addView(fila)

            view.post {
                val w = track.width
                ValueAnimator.ofInt(0, (w * h.abandono / 100f).toInt()).apply {
                    duration     = 700
                    interpolator = DecelerateInterpolator()
                    addUpdateListener {
                        val lp = fill.layoutParams as LinearLayout.LayoutParams
                        lp.width = it.animatedValue as Int
                        fill.layoutParams = lp
                    }
                    start()
                }
            }
        }
    }

    private fun dibujarGraficoActividad(view: View, datos: List<DatosBarra>) {
        val context = context ?: return
        val container = view.findViewById<LinearLayout>(R.id.llActividadChart) ?: return
        container.removeAllViews()

        val maxVal   = datos.maxOf { it.valor }
        val altoPx   = dpToPx(56)

        val colorSeleccionado = ContextCompat.getColor(context, R.color.a78bfa)
        val colorPico = ContextCompat.getColor(context, R.color.colorMeta)
        val colorVacio = ContextCompat.getColor(context, R.color.progressBarVacia)
        val colorTexto1 = ContextCompat.getColor(context, R.color.texto1Admin)
        val colorWhite = ContextCompat.getColor(context, R.color.white)

        datos.forEachIndexed { idx, bar ->
            val esPico = bar.valor == maxVal

            val col = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                isClickable  = true
                isFocusable  = true
                setOnClickListener {
                    diaSeleccionado = idx
                    dibujarGraficoActividad(view, datos)
                    actualizarInfoDia(view, datos)
                }
            }

            val tvPct = TextView(context).apply {
                text      = "${bar.valor}%"
                textSize = 8f
                setTextColor(if (esPico || idx == diaSeleccionado) colorWhite else Color.TRANSPARENT)
                gravity  = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(2) }
            }

            val barView = View(context).apply {
                val colorBarra = when {
                    idx == diaSeleccionado -> colorSeleccionado
                    esPico                 -> colorPico
                    else                   -> colorVacio
                }
                setBackgroundColor(colorBarra)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0
                ).also { it.marginStart = dpToPx(2); it.marginEnd = dpToPx(2) }
            }

            val tvDia = TextView(context).apply {
                text     = bar.label
                textSize = 9f
                setTextColor(if (idx == diaSeleccionado || esPico) colorPico else colorTexto1)
                gravity  = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dpToPx(4) }
            }

            col.addView(tvPct)
            col.addView(barView)
            col.addView(tvDia)
            container.addView(col)

            val targetH = (bar.valor.toFloat() / maxVal * altoPx).toInt()
            ValueAnimator.ofInt(0, targetH).apply {
                duration     = 500
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val lp = barView.layoutParams as LinearLayout.LayoutParams
                    lp.height = it.animatedValue as Int
                    barView.layoutParams = lp
                }
                start()
            }
        }
    }

    private fun actualizarInfoDia(view: View, datos: List<DatosBarra>) {
        val d = datos.getOrNull(diaSeleccionado) ?: return
        view.findViewById<TextView>(R.id.tvDiaSeleccionado)?.text   = d.label
        view.findViewById<TextView>(R.id.tvCompletacionDia)?.text   = "${d.valor}%"
        view.findViewById<TextView>(R.id.tvSesionesDia)?.text       = fmt(d.sesiones)
    }

    private fun configurarActividadDia(view: View) {
        view.post {
            dibujarGraficoActividad(view, datosMes)
            actualizarInfoDia(view, datosMes)
        }
    }

    private fun configurarProgresoRangos(view: View) {
        val context = context ?: return
        val colorVacio = ContextCompat.getColor(context, R.color.progressBarVacia)
        val colorMeta = ContextCompat.getColor(context, R.color.colorMeta)

        view.findViewById<DonutChartView>(R.id.donutProgreso)?.apply {
            setProgress(73, 100)
            setColors(trackColor = colorVacio, progressColor = colorMeta)
            setCenterText("73%")
        }

        val llLeyenda = view.findViewById<LinearLayout>(R.id.llLeyendaProgreso)
        llLeyenda?.removeAllViews()
        rangos.forEach { r ->
            val fila = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(5) }
            }
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(6), dpToPx(6))
                    .also { it.marginEnd = dpToPx(6) }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(context, r.colorRes))
                }
            }
            val tvLabel = TextView(context).apply {
                text     = r.rango
                textSize = 9f
                setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvVal = TextView(context).apply {
                text     = fmt(r.cantidad)
                textSize = 9f
                setTextColor(ContextCompat.getColor(context, r.colorRes))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            fila.addView(dot)
            fila.addView(tvLabel)
            fila.addView(tvVal)
            llLeyenda?.addView(fila)
        }

        val llDist = view.findViewById<LinearLayout>(R.id.llDistribucion)
        llDist?.removeAllViews()
        val totalRangos = rangos.sumOf { it.cantidad }
        rangos.forEach { r ->
            val fila = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(8) }
            }
            val cabecera = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(4) }
            }
            val pct = (r.cantidad * 100f / totalRangos).toInt()

            val tvLabel = TextView(context).apply {
                text     = r.rango
                textSize = 10f
                setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvInfo = TextView(context).apply {
                text     = "${fmt(r.cantidad)}   $pct%"
                textSize = 10f
                setTextColor(ContextCompat.getColor(context, r.colorRes))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cabecera.addView(tvLabel)
            cabecera.addView(tvInfo)

            val track = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(3)
                )
                setBackgroundColor(colorVacio)
            }
            val fill = View(context).apply {
                setBackgroundColor(ContextCompat.getColor(context, r.colorRes))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            }
            track.addView(fill)

            fila.addView(cabecera)
            fila.addView(track)
            llDist?.addView(fila)

            view.post {
                val w = track.width
                ValueAnimator.ofInt(0, (w * pct / 100f).toInt()).apply {
                    duration     = 800
                    interpolator = DecelerateInterpolator()
                    addUpdateListener {
                        val lp = fill.layoutParams as LinearLayout.LayoutParams
                        lp.width = it.animatedValue as Int
                        fill.layoutParams = lp
                    }
                    start()
                }
            }
        }

        view.findViewById<TextView>(R.id.tvPromedioGeneral)?.text = getString(R.string._73)
        view.findViewById<TextView>(R.id.tvTop10)?.text           = getString(R.string.percent_95)
        view.findViewById<TextView>(R.id.tvEnRiesgo)?.text        = getString(R.string.count_221)
    }

    private fun configurarTendenciaMensual(view: View) {
        val context = context ?: return
        view.findViewById<TextView>(R.id.tvCambioTendencia)?.text = getString(R.string.tendencia_positiva_demo)
        view.findViewById<TextView>(R.id.tvUsuariosFeb)?.text     = fmt(totalUsuarios)

        val llTendencia = view.findViewById<LinearLayout>(R.id.llTendenciaChart) ?: return
        llTendencia.removeAllViews()

        val meses   = listOf("Sep", "Oct", "Nov", "Dic", "Ene", "Feb")
        val maxVal  = tendenciaMensual.max()
        val altoPx  = dpToPx(50)

        val colorPico = ContextCompat.getColor(context, R.color.colorMeta)
        val colorVacio = ContextCompat.getColor(context, R.color.tendenciaVacia)
        val colorTexto1 = ContextCompat.getColor(context, R.color.texto1Admin)
        val colorWhite = ContextCompat.getColor(context, R.color.white)

        tendenciaMensual.forEachIndexed { i, valor ->
            val esUltimo = i == tendenciaMensual.lastIndex
            val col = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }
            val tvPct = TextView(context).apply {
                text     = "$valor%"
                textSize = 8f
                setTextColor(if (esUltimo) colorWhite else Color.TRANSPARENT)
                gravity  = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(2) }
            }
            val barView = View(context).apply {
                setBackgroundColor(if (esUltimo) colorPico else colorVacio)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0
                ).also { it.marginStart = dpToPx(2); it.marginEnd = dpToPx(2) }
            }
            val tvMes = TextView(context).apply {
                text     = meses[i]
                textSize = 9f
                setTextColor(if (esUltimo) colorPico else colorTexto1)
                gravity  = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dpToPx(4) }
            }
            col.addView(tvPct)
            col.addView(barView)
            col.addView(tvMes)
            llTendencia.addView(col)

            val targetH = (valor.toFloat() / maxVal * altoPx).toInt()
            ValueAnimator.ofInt(0, targetH).apply {
                duration     = 600
                startDelay   = (i * 60).toLong()
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val lp = barView.layoutParams as LinearLayout.LayoutParams
                    lp.height = it.animatedValue as Int
                    barView.layoutParams = lp
                }
                start()
            }
        }
    }

    private fun configurarExportar(view: View) {
        view.findViewById<Button>(R.id.btnExportarPDF)?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.generando_pdf), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.btnExportarCSV)?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.exportando_csv), Toast.LENGTH_SHORT).show()
        }
    }

    private fun fmt(n: Int) = String.format("%,d", n).replace(',', '.')
    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}