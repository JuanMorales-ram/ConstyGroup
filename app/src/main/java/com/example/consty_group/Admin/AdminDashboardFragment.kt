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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.consty_group.R
import com.example.consty_group.data.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AdminDashboardFragment
 * ──────────────────────
 * Pantalla principal del panel de administrador.
 * Carga todos los datos desde Supabase en paralelo y los renderiza.
 */
class AdminDashboardFragment : Fragment() {

    // ── Estado ───────────────────────────────────────────────────────────────

    private var mostrandoSemana = true
    private var datos: DatosDashboard? = null

    data class DatosDashboard(
        val totalUsuarios: Int,
        val tasaCompletacion: Int,
        val activosHoy: Int,
        val activosSemana: Int,
        val activosMes: Int,
        val tendenciaSemanal: List<AdminRepository.DatoGrafico>,
        val tendenciaMensual: List<AdminRepository.DatoGrafico>,
        val habitosPopulares: List<AdminRepository.HabitoStats>
    )

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarFecha(view)
        mostrarEstadoCargando(view)

        // Cargar todos los datos en paralelo
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val totalUsuarios    = async { AdminRepository.contarTotalUsuarios() }
                val tasaCompletacion = async { AdminRepository.calcularTasaCompletacion() }
                val activosHoy       = async { AdminRepository.contarActivosHoy() }
                val activosSemana    = async { AdminRepository.contarActivosSemana() }
                val activosMes       = async { AdminRepository.contarActivosMes() }
                val tendSemanal      = async { AdminRepository.obtenerTendenciaSemanal() }
                val tendMensual      = async { AdminRepository.obtenerTendenciaMensual() }
                val habitosTop       = async { AdminRepository.obtenerHabitosPopulares() }

                datos = DatosDashboard(
                    totalUsuarios    = totalUsuarios.await(),
                    tasaCompletacion = tasaCompletacion.await(),
                    activosHoy       = activosHoy.await(),
                    activosSemana    = activosSemana.await(),
                    activosMes       = activosMes.await(),
                    tendenciaSemanal = tendSemanal.await(),
                    tendenciaMensual = tendMensual.await(),
                    habitosPopulares = habitosTop.await()
                )

                // Renderizar con datos reales
                configurarTarjetas(view)
                configurarDonut(view)
                configurarProgresoBars(view)
                configurarTendencia(view)
                configurarHabitosPopulares(view)
                configurarFooter(view)

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Error cargando datos: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ── Placeholders mientras carga ───────────────────────────────────────────

    private fun mostrarEstadoCargando(view: View) {
        view.findViewById<TextView>(R.id.tvTotalUsuarios).text   = "..."
        view.findViewById<TextView>(R.id.tvTasaCompletacion).text = "..."
        view.findViewById<TextView>(R.id.tvActivosHoy).text       = "..."
        view.findViewById<TextView>(R.id.tvActivosSemana).text    = "..."
        view.findViewById<TextView>(R.id.tvCompletacionActual).text = "..."
        view.findViewById<TextView>(R.id.tvPctActivosHoy).text    = ""
        view.findViewById<TextView>(R.id.tvPctActivosSemana).text = ""
        view.findViewById<TextView>(R.id.tvActivosHoyLabel).text  = ""
        view.findViewById<TextView>(R.id.tvActivosSemanaLabel).text = ""
        view.findViewById<TextView>(R.id.tvActivosMesLabel).text  = ""
    }

    // ── Fecha ─────────────────────────────────────────────────────────────────

    private fun configurarFecha(view: View) {
        val sdf   = SimpleDateFormat("EEEE, d 'de' MMMM · yyyy", Locale("es", "ES"))
        val fecha = sdf.format(Date()).replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.tvFechaAdmin).text = fecha
    }

    // ── Tarjetas superiores ───────────────────────────────────────────────────

    private fun configurarTarjetas(view: View) {
        val d = datos ?: return

        view.findViewById<TextView>(R.id.tvTotalUsuarios).text =
            fmt(d.totalUsuarios)
        view.findViewById<TextView>(R.id.tvTasaCompletacion).text =
            "${d.tasaCompletacion}%"
        view.findViewById<TextView>(R.id.tvActivosHoy).text =
            "${d.activosHoy}"
        view.findViewById<TextView>(R.id.tvPctActivosHoy).text =
            "${pct(d.activosHoy, d.totalUsuarios)}% del total"
        view.findViewById<TextView>(R.id.tvActivosSemana).text =
            "${d.activosSemana}"
        view.findViewById<TextView>(R.id.tvPctActivosSemana).text =
            "${pct(d.activosSemana, d.totalUsuarios)}% del total"
        view.findViewById<TextView>(R.id.tvCompletacionActual).text =
            "${d.tasaCompletacion}%"
    }

    // ── Donut chart ───────────────────────────────────────────────────────────

    private fun configurarDonut(view: View) {
        val d       = datos ?: return
        val context = context ?: return
        view.findViewById<DonutChartView>(R.id.donutChart).apply {
            setProgress(d.tasaCompletacion, 100)
            setColors(
                trackColor    = ContextCompat.getColor(context, R.color.progressBarVacia),
                progressColor = ContextCompat.getColor(context, R.color._2dd4bf_)
            )
            setCenterText("${d.tasaCompletacion}%")
        }
    }

    // ── Progress bars de usuarios activos ─────────────────────────────────────

    private fun configurarProgresoBars(view: View) {
        val d = datos ?: return

        val pctHoy = pct(d.activosHoy,    d.totalUsuarios)
        val pctSem = pct(d.activosSemana, d.totalUsuarios)
        val pctMes = pct(d.activosMes,    d.totalUsuarios)

        view.findViewById<TextView>(R.id.tvActivosHoyLabel).text =
            "${d.activosHoy} / ${fmt(d.totalUsuarios)}"
        animarProgressBar(view.findViewById(R.id.pbActivosHoy), pctHoy)

        view.findViewById<TextView>(R.id.tvActivosSemanaLabel).text =
            "${d.activosSemana} / ${fmt(d.totalUsuarios)}"
        animarProgressBar(view.findViewById(R.id.pbActivosSemana), pctSem)

        view.findViewById<TextView>(R.id.tvActivosMesLabel).text =
            "${fmt(d.activosMes)} / ${fmt(d.totalUsuarios)}"
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

    // ── Gráfico de tendencia ──────────────────────────────────────────────────

    private fun configurarTendencia(view: View) {
        val d       = datos ?: return
        val tabSemana = view.findViewById<TextView>(R.id.tabSemana)
        val tabMes    = view.findViewById<TextView>(R.id.tabMes)

        tabSemana.setOnClickListener {
            mostrandoSemana = true
            actualizarTabs(tabSemana, tabMes)
            dibujarGrafico(view, d.tendenciaSemanal)
        }
        tabMes.setOnClickListener {
            mostrandoSemana = false
            actualizarTabs(tabMes, tabSemana)
            dibujarGrafico(view, d.tendenciaMensual)
        }

        actualizarTabs(tabSemana, tabMes)
        dibujarGrafico(view, d.tendenciaSemanal)
    }

    private fun actualizarTabs(activo: TextView, inactivo: TextView) {
        val context = context ?: return
        activo.setBackgroundResource(R.drawable.bg_tab_selected)
        activo.setTextColor(ContextCompat.getColor(context, R.color.white))
        inactivo.setBackgroundResource(R.drawable.bg_tab_unselected)
        inactivo.setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
    }

    private fun dibujarGrafico(view: View, datos: List<AdminRepository.DatoGrafico>) {
        val context   = context ?: return
        val container = view.findViewById<LinearLayout>(R.id.llChartArea)
        container.removeAllViews()

        // Evitar crash si la lista viene vacía
        if (datos.isEmpty()) {
            view.findViewById<TextView>(R.id.tvPicoActividad).text = "Sin datos aún"
            return
        }

        val maxVal    = datos.maxOf { it.valor }.takeIf { it > 0 } ?: 1
        val altoMaxPx = dpToPx(60)
        val pico      = datos.maxByOrNull { it.valor } ?: datos.first()

        val colorMeta   = ContextCompat.getColor(context, R.color.colorMeta)
        val colorVacio  = ContextCompat.getColor(context, R.color.progressBarVacia)
        val colorTexto1 = ContextCompat.getColor(context, R.color.texto1Admin)
        val colorWhite  = ContextCompat.getColor(context, R.color.white)

        datos.forEach { bar ->
            val esPico = bar.valor == maxVal

            val col = LinearLayout(context).apply {
                orientation  = LinearLayout.VERTICAL
                gravity      = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
            }

            val tvPct = TextView(context).apply {
                text      = "${bar.valor}%"
                textSize  = 8f
                setTextColor(if (esPico) colorWhite else Color.TRANSPARENT)
                gravity   = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(2) }
            }

            val altoPx  = ((bar.valor.toFloat() / maxVal) * altoMaxPx).toInt().coerceAtLeast(2)
            val barView = View(context).apply {
                setBackgroundColor(if (esPico) colorMeta else colorVacio)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0
                ).also {
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
                setTextColor(if (esPico) colorMeta else colorTexto1)
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
            "⚡ Pico de actividad: ${pico.label} ${pico.valor}% — mejor " +
                    "${if (mostrandoSemana) "día de la semana" else "semana del mes"}"
    }

    // ── Hábitos populares ─────────────────────────────────────────────────────

    private fun configurarHabitosPopulares(view: View) {
        val d         = datos ?: return
        val container = view.findViewById<LinearLayout>(R.id.llHabitosPopulares)
        container.removeAllViews()

        if (d.habitosPopulares.isEmpty()) {
            container.addView(TextView(requireContext()).apply {
                text      = "Sin datos aún"
                textSize  = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.texto1Admin))
            })
            return
        }

        d.habitosPopulares.forEachIndexed { index, habito ->
            val esUltimo = index == d.habitosPopulares.lastIndex
            container.addView(crearHabitoItem(habito, esUltimo, index))
        }
    }

    private fun crearHabitoItem(
        habito: AdminRepository.HabitoStats,
        esUltimo: Boolean,
        posicion: Int
    ): LinearLayout {
        val context = context ?: return LinearLayout(requireContext())
        val color   = ContextCompat.getColor(context, habito.colorRes)

        // Fila principal
        val fila = LinearLayout(context).apply {
            orientation  = LinearLayout.HORIZONTAL
            gravity      = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Rank (emoji medalla o número)
        val tvRank = TextView(context).apply {
            text     = habito.rank
            textSize = if (posicion == 0) 13f else 10f
            setTextColor(
                if (posicion == 0) ContextCompat.getColor(context, R.color.medioAdmin)
                else ContextCompat.getColor(context, R.color.texto1Admin)
            )
            gravity      = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(22), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        // Caja emoji
        val emojiBox = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_card_dark)
            layoutParams = LinearLayout.LayoutParams(dpToPx(30), dpToPx(30)).also {
                it.marginEnd = dpToPx(10)
            }
        }
        emojiBox.addView(TextView(context).apply {
            text     = habito.emoji
            textSize = 14f
        })

        // Columna de info (nombre + barra + usuarios)
        val colInfo = LinearLayout(context).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvNombre = TextView(context).apply {
            text     = habito.nombre
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Barra de progreso manual (track + fill)
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

        // Animar el fill después de que el track esté medido
        track.post {
            val trackWidth = track.width
            if (trackWidth > 0) {
                ValueAnimator.ofInt(0, (trackWidth * habito.porcentaje / 100f).toInt()).apply {
                    duration     = 800
                    startDelay   = (posicion * 80).toLong()
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

        val tvUsuarios = TextView(context).apply {
            text     = "${fmt(habito.usuarios)} usuarios"
            textSize = 10f
            setTextColor(ContextCompat.getColor(context, R.color.texto1Admin))
        }

        colInfo.addView(tvNombre)
        colInfo.addView(track)
        colInfo.addView(tvUsuarios)

        // Porcentaje a la derecha
        val tvPct = TextView(context).apply {
            text     = "${habito.porcentaje}%"
            textSize = 13f
            setTextColor(color)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity      = Gravity.END or Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(dpToPx(40), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        fila.addView(tvRank)
        fila.addView(emojiBox)
        fila.addView(colInfo)
        fila.addView(tvPct)

        // Si no es el último, envolver en un wrapper con divisor
        if (!esUltimo) {
            val wrapper = LinearLayout(context).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(10) }
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

    // ── Footer ────────────────────────────────────────────────────────────────

    private fun configurarFooter(view: View) {
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        view.findViewById<TextView>(R.id.tvUltimaActualizacion).text =
            "Última actualización: $hora"
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Formato de número con puntos: 1.284 */
    private fun fmt(n: Int) = String.format("%,d", n).replace(',', '.')

    /** Porcentaje seguro (evita división por cero) */
    private fun pct(parte: Int, total: Int) =
        if (total > 0) (parte * 100) / total else 0

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}