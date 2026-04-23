package com.example.consty_group.admin

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AdminDashboardFragment
 * ──────────────────────
 * Pantalla principal del panel de administrador.
 */
class AdminDashboardFragment : Fragment() {

    private val totalUsuarios    = 1284
    private val tasaCompletacion = 73
    private val activosHoy       = 347
    private val activosSemana    = 892
    private val activosMes       = 1100

    data class HabitoPopular(
        val rank: String,
        val emoji: String,
        val nombre: String,
        val usuarios: Int,
        val porcentaje: Int,
        val colorRes: Int
    )

    private val habitosPopulares = listOf(
        HabitoPopular("⚡", "💧", "Beber agua",  1024, 90, R.color._2dd4bf_),
        HabitoPopular("#2", "🏋️", "Ejercicio",   891,  76, R.color.bajoAdmin),
        HabitoPopular("#3", "🧘", "Meditación",  743,  82, R.color.a78bfa),
        HabitoPopular("#4", "📖", "Leer 30 min", 612,  61, R.color.progressEstaSemana),
        HabitoPopular("#5", "🌙", "Dormir 8h",   528,  54, R.color.medioAdmin)
    )

    // Datos del gráfico de tendencia
    data class BarData(val label: String, val valor: Int)

    private val datosSemana = listOf(
        BarData("L", 62), BarData("M", 58), BarData("X", 71),
        BarData("J", 67), BarData("V", 88), BarData("S", 74), BarData("D", 55)
    )
    private val datosMes = listOf(
        BarData("S1", 65), BarData("S2", 70), BarData("S3", 75), BarData("S4", 88)
    )

    private var mostrandoSemana = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarFecha(view)
        configurarTarjetas(view)
        configurarDonut(view)
        configurarProgresoBars(view)
        configurarTendencia(view)
        configurarHabitosPopulares(view)
        configurarFooter(view)
    }

    private fun configurarFecha(view: View) {
        val sdf   = SimpleDateFormat("EEEE, d 'de' MMMM · yyyy", Locale("es", "ES"))
        val fecha = sdf.format(Date()).replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.tvFechaAdmin).text = fecha
    }

    private fun configurarTarjetas(view: View) {
        view.findViewById<TextView>(R.id.tvTotalUsuarios).text =
            String.format("%,d", totalUsuarios).replace(',', '.')

        view.findViewById<TextView>(R.id.tvTasaCompletacion).text = getString(R.string._73)

        view.findViewById<TextView>(R.id.tvActivosHoy).text = "$activosHoy"
        val pctHoy = (activosHoy * 100) / totalUsuarios
        view.findViewById<TextView>(R.id.tvPctActivosHoy).text = "$pctHoy% del total"

        view.findViewById<TextView>(R.id.tvActivosSemana).text = "$activosSemana"
        val pctSem = (activosSemana * 100) / totalUsuarios
        view.findViewById<TextView>(R.id.tvPctActivosSemana).text = "$pctSem% del total"

        view.findViewById<TextView>(R.id.tvCompletacionActual).text = getString(R.string._73)
    }

    private fun configurarDonut(view: View) {
        val context = context ?: return
        view.findViewById<DonutChartView>(R.id.donutChart).apply {
            setProgress(tasaCompletacion, 100)
            setColors(
                trackColor    = ContextCompat.getColor(context, R.color.progressBarVacia),
                progressColor = ContextCompat.getColor(context, R.color._2dd4bf_)
            )
            setCenterText("$tasaCompletacion%")
        }
    }

    private fun configurarProgresoBars(view: View) {
        val pctHoy = (activosHoy    * 100) / totalUsuarios
        val pctSem = (activosSemana * 100) / totalUsuarios
        val pctMes = (activosMes    * 100) / totalUsuarios

        view.findViewById<TextView>(R.id.tvActivosHoyLabel).text =
            "$activosHoy / ${fmt(totalUsuarios)}"
        animarProgressBar(view.findViewById(R.id.pbActivosHoy), pctHoy)

        view.findViewById<TextView>(R.id.tvActivosSemanaLabel).text =
            "$activosSemana / ${fmt(totalUsuarios)}"
        animarProgressBar(view.findViewById(R.id.pbActivosSemana), pctSem)

        view.findViewById<TextView>(R.id.tvActivosMesLabel).text =
            "${fmt(activosMes)} / ${fmt(totalUsuarios)}"
        animarProgressBar(view.findViewById(R.id.pbActivosMes), pctMes)
    }

    private fun animarProgressBar(pb: ProgressBar, target: Int) {
        ValueAnimator.ofInt(0, target).apply {
            duration     = 900
            interpolator = DecelerateInterpolator()
            addUpdateListener { pb.progress = it.animatedValue as Int }
            start()
        }
    }

    private fun configurarTendencia(view: View) {
        val tabSemana = view.findViewById<TextView>(R.id.tabSemana)
        val tabMes    = view.findViewById<TextView>(R.id.tabMes)

        tabSemana.setOnClickListener {
            mostrandoSemana = true
            actualizarTabs(tabSemana, tabMes)
            dibujarGrafico(view, datosSemana)
        }
        tabMes.setOnClickListener {
            mostrandoSemana = false
            actualizarTabs(tabMes, tabSemana)
            dibujarGrafico(view, datosMes)
        }

        dibujarGrafico(view, datosSemana)
    }

    private fun actualizarTabs(activo: TextView, inactivo: TextView) {
        val context = context ?: return
        activo.setBackgroundResource(R.drawable.bg_tab_selected)
        activo.setTextColor(ContextCompat.getColor(context, R.color.white))
        inactivo.setBackgroundResource(R.drawable.bg_tab_unselected)
        inactivo.setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
    }

    private fun dibujarGrafico(view: View, datos: List<BarData>) {
        val context = context ?: return
        val container = view.findViewById<LinearLayout>(R.id.llChartArea)
        container.removeAllViews()

        val maxVal = datos.maxOf { it.valor }
        val altoMaxPx = dpToPx(60)
        val pico = datos.first { it.valor == maxVal }

        val colorMeta = ContextCompat.getColor(context, R.color.colorMeta)
        val colorVacio = ContextCompat.getColor(context, R.color.progressBarVacia)
        val colorTexto1 = ContextCompat.getColor(context, R.color.texto1Admin)
        val colorWhite = ContextCompat.getColor(context, R.color.white)

        datos.forEach { bar ->
            val col = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val tvPct = TextView(context).apply {
                text      = "${bar.valor}%"
                textSize  = 8f
                setTextColor(if (bar.valor == maxVal) colorWhite else Color.TRANSPARENT)
                gravity   = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(2) }
            }

            val altoPx = ((bar.valor.toFloat() / maxVal) * altoMaxPx).toInt()
            val barView = View(context).apply {
                setBackgroundColor(if (bar.valor == maxVal) colorMeta else colorVacio)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).also {
                    it.marginStart = dpToPx(2)
                    it.marginEnd   = dpToPx(2)
                }
            }

            val anim = ValueAnimator.ofInt(0, altoPx).apply {
                duration     = 600
                interpolator = DecelerateInterpolator()
                addUpdateListener { anim ->
                    val lp = barView.layoutParams as LinearLayout.LayoutParams
                    lp.height = anim.animatedValue as Int
                    barView.layoutParams = lp
                }
            }

            val tvDia = TextView(context).apply {
                text     = bar.label
                textSize = 9f
                setTextColor(if (bar.valor == maxVal) colorMeta else colorTexto1)
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

            anim.start()
        }

        view.findViewById<TextView>(R.id.tvPicoActividad).text =
            "⚡ Pico de actividad: ${pico.label} ${pico.valor}% — mejor ${if (mostrandoSemana) "día de la semana" else "semana del mes"}"
    }

    private fun configurarHabitosPopulares(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.llHabitosPopulares)
        container.removeAllViews()

        habitosPopulares.forEachIndexed { index, habito ->
            val itemView = crearHabitoItem(habito, index == habitosPopulares.lastIndex)
            container.addView(itemView)
        }
    }

    private fun crearHabitoItem(habito: HabitoPopular, esUltimo: Boolean): LinearLayout {
        val context = context ?: return LinearLayout(requireContext())
        val color = ContextCompat.getColor(context, habito.colorRes)

        val fila = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                if (!esUltimo) it.bottomMargin = dpToPx(10)
            }
        }

        val tvRank = TextView(context).apply {
            text      = habito.rank
            textSize  = if (index(habito) == 0) 13f else 10f
            setTextColor(if (index(habito) == 0) ContextCompat.getColor(context, R.color.medioAdmin) else ContextCompat.getColor(context, R.color.texto1Admin))
            gravity   = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(22), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val emojiBox = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_card_dark)
            layoutParams = LinearLayout.LayoutParams(dpToPx(30), dpToPx(30)).also {
                it.marginEnd = dpToPx(10)
            }
        }
        val tvEmoji = TextView(context).apply {
            text     = habito.emoji
            textSize = 14f
        }
        emojiBox.addView(tvEmoji)

        val colInfo = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvNombre = TextView(context).apply {
            text     = habito.nombre
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val track = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(3)
            ).also { it.topMargin = dpToPx(5); it.bottomMargin = dpToPx(4) }
            setBackgroundColor(ContextCompat.getColor(context, R.color.progressBarVacia))
        }
        val fill = View(context).apply {
            setBackgroundColor(color)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        track.addView(fill)

        view?.post {
            val trackWidth = track.width
            ValueAnimator.ofInt(0, (trackWidth * habito.porcentaje / 100f).toInt()).apply {
                duration     = 800
                startDelay   = (index(habito) * 80).toLong()
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val lp = fill.layoutParams as LinearLayout.LayoutParams
                    lp.width = it.animatedValue as Int
                    fill.layoutParams = lp
                }
                start()
            }
        }

        val tvUsuarios = TextView(context).apply {
            text     = "${fmt(habito.usuarios)} usuarios"
            textSize = 10f
            setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
        }

        colInfo.addView(tvNombre)
        colInfo.addView(track)
        colInfo.addView(tvUsuarios)

        val tvPct = TextView(context).apply {
            text      = "${habito.porcentaje}%"
            textSize  = 13f
            setTextColor(color)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity   = Gravity.END or Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(dpToPx(40), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        fila.addView(tvRank)
        fila.addView(emojiBox)
        fila.addView(colInfo)
        fila.addView(tvPct)

        if (!esUltimo) {
            val wrapper = LinearLayout(context).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val divider = View(context).apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.progressBarVacia))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                ).also { it.topMargin = dpToPx(10) }
            }
            wrapper.addView(fila)
            wrapper.addView(divider)
            return wrapper
        }

        return fila
    }

    private fun configurarFooter(view: View) {
        view.findViewById<TextView>(R.id.tvUltimaActualizacion).text =
            getString(R.string.ltima_actualizaci_)
    }

    private fun fmt(n: Int) = String.format("%,d", n).replace(',', '.')

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun index(h: HabitoPopular) = habitosPopulares.indexOf(h)
}