package com.example.consty_group.admin

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.consty_group.R

// ─────────────────────────────────────────────
// MODELOS
// ─────────────────────────────────────────────

data class HabitoAbandono(
    val emoji: String,
    val nombre: String,
    val usuarios: Int,
    val porcentaje: Int,
    val tendencia: Int,   // positivo = baja, negativo = sube
    val colorHex: String
)

data class DatosDia(
    val nombre: String,
    val porcentaje: Int,
    val sesiones: Int
)

data class RangoProg(
    val etiqueta: String,
    val usuarios: Int,
    val porcentaje: Int,
    val colorHex: String
)

// ─────────────────────────────────────────────
// FRAGMENT
// ─────────────────────────────────────────────

/**
 * EstadisticasReportesFragment
 * ────────────────────────────
 * Pantalla de estadísticas y reportes del panel admin.
 * Archivo nuevo — NO modifica ningún archivo existente.
 */
class EstadisticasReportesFragment : Fragment() {

    private val habitosAbandono = listOf(
        HabitoAbandono("🌙", "Dormir 8h",   528,  46, -6, "#F87171"),
        HabitoAbandono("📖", "Leer 30 min", 612,  39, -5, "#FB923C"),
        HabitoAbandono("🏋️", "Ejercicio",   891,  24,  2, "#FACC15"),
        HabitoAbandono("🧘", "Meditación",  743,  18, -3, "#818CF8"),
        HabitoAbandono("💧", "Beber agua",  1024, 10,  4, "#2DD4BF")
    )

    private val datosSemana = listOf(
        DatosDia("Lun", 62, 850),
        DatosDia("Mar", 70, 920),
        DatosDia("Mié", 55, 780),
        DatosDia("Jue", 75, 1050),
        DatosDia("Vie", 89, 1168),
        DatosDia("Sáb", 66, 900),
        DatosDia("Dom", 42, 600)
    )

    private val distribucion = listOf(
        RangoProg("0–20%",    87,  7,  "#F87171"),
        RangoProg("21–40%",  134, 10,  "#FB923C"),
        RangoProg("41–60%",  298, 23,  "#FACC15"),
        RangoProg("61–80%",  412, 32,  "#818CF8"),
        RangoProg("81–100%", 353, 27,  "#2DD4BF")
    )

    private var diaSeleccionado = 4  // Viernes por defecto

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_estadisticas_reportes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarTabs(view)
        configurarHabitosAbandono(view)
        configurarBarChart(view)
        configurarDistribucion(view)
        configurarExportar(view)
    }

    // ── Tabs período ──
    private fun configurarTabs(view: View) {
        val tabs = listOf(
            view.findViewById<TextView>(R.id.tabSemana),
            view.findViewById<TextView>(R.id.tabMes),
            view.findViewById<TextView>(R.id.tabAnio)
        )
        tabs.forEachIndexed { i, tab ->
            tab.setOnClickListener {
                tabs.forEach { t ->
                    t.setBackgroundResource(R.drawable.bg_filter_inactive)
                    t.setTextColor(Color.parseColor("#8888AA"))
                }
                tab.setBackgroundResource(R.drawable.bg_filter_active)
                tab.setTextColor(Color.WHITE)
                // TODO: cargar datos del período seleccionado
            }
        }
    }

    // ── Hábitos con mayor abandono ──
    private fun configurarHabitosAbandono(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.llHabitosAbandono)
        container.removeAllViews()

        habitosAbandono.forEach { h ->
            val item = layoutInflater.inflate(
                R.layout.item_habito_abandono, container, false
            )
            item.findViewById<TextView>(R.id.tvHabitoEmoji2).text  = h.emoji
            item.findViewById<TextView>(R.id.tvHabitoNombre2).text = h.nombre
            item.findViewById<TextView>(R.id.tvHabitoUsuarios2).text =
                "${String.format("%,d", h.usuarios).replace(',', '.')} usuarios afectados"

            val tvPct = item.findViewById<TextView>(R.id.tvHabitoPct2)
            tvPct.text = "${h.porcentaje}%"
            tvPct.setTextColor(Color.parseColor(h.colorHex))

            val tvTrend = item.findViewById<TextView>(R.id.tvHabitoTrend)
            tvTrend.text  = if (h.tendencia < 0) "▼ ${Math.abs(h.tendencia)}%" else "▲ ${h.tendencia}%"
            tvTrend.setTextColor(
                if (h.tendencia < 0) Color.parseColor("#F87171")
                else Color.parseColor("#22C55E")
            )

            val pb = item.findViewById<ProgressBar>(R.id.pbHabitoAbandono)
            pb.progress = h.porcentaje
            pb.progressTintList =
                ColorStateList.valueOf(Color.parseColor(h.colorHex))

            container.addView(item)
        }
    }

    // ── Gráfica de barras por día ──
    private fun configurarBarChart(view: View) {
        val chart = view.findViewById<BarChartView>(R.id.barChartDias)
        chart.setDatos(datosSemana, diaSeleccionado)
        chart.setOnBarClickListener { index ->
            diaSeleccionado = index
            val dia = datosSemana[index]
            view.findViewById<TextView>(R.id.tvDiaSeleccionado).text = dia.nombre
            view.findViewById<TextView>(R.id.tvCompletacionDia).text = "${dia.porcentaje}%"
            view.findViewById<TextView>(R.id.tvSesionesDia).text =
                String.format("%,d", dia.sesiones).replace(',', '.')
            chart.setDatos(datosSemana, index)
        }
        // Valores iniciales
        val diaDefault = datosSemana[diaSeleccionado]
        view.findViewById<TextView>(R.id.tvDiaSeleccionado).text = diaDefault.nombre
        view.findViewById<TextView>(R.id.tvCompletacionDia).text = "${diaDefault.porcentaje}%"
        view.findViewById<TextView>(R.id.tvSesionesDia).text =
            String.format("%,d", diaDefault.sesiones).replace(',', '.')
    }

    // ── Distribución de progreso ──
    private fun configurarDistribucion(view: View) {
        val llDist = view.findViewById<LinearLayout>(R.id.llDistribucion)
        val llLey  = view.findViewById<LinearLayout>(R.id.llLeyendaProgreso)
        llDist.removeAllViews()
        llLey.removeAllViews()

        distribucion.forEach { r ->
            // Leyenda del donut
            val legView = layoutInflater.inflate(R.layout.item_leyenda, llLey, false)
            legView.findViewById<View>(R.id.viewLegDot)
                .setBackgroundColor(Color.parseColor(r.colorHex))
            legView.findViewById<TextView>(R.id.tvLegLabel).text = r.etiqueta
            legView.findViewById<TextView>(R.id.tvLegVal).apply {
                text = "${r.usuarios}"
                setTextColor(Color.parseColor(r.colorHex))
            }
            llLey.addView(legView)

            // Barra de distribución
            val distView = layoutInflater.inflate(R.layout.item_dist_bar, llDist, false)
            distView.findViewById<TextView>(R.id.tvDistLabel).apply {
                text = r.etiqueta
                setTextColor(Color.parseColor(r.colorHex))
            }
            distView.findViewById<TextView>(R.id.tvDistInfo).text =
                "${String.format("%,d", r.usuarios).replace(',', '.')} usuarios · ${r.porcentaje}%"
            val pb = distView.findViewById<ProgressBar>(R.id.pbDist)
            pb.progress = r.porcentaje * 3   // escala visual
            pb.progressTintList =
                ColorStateList.valueOf(Color.parseColor(r.colorHex))
            llDist.addView(distView)
        }

        // Donut multicolor
        view.findViewById<DonutChartView>(R.id.donutProgreso).apply {
            setProgress(73, 100)
            setColors(Color.parseColor("#1E1E3A"), Color.parseColor("#2DD4BF"))
            setCenterText("73%")
        }
    }

    // ── Exportar ──
    private fun configurarExportar(view: View) {
        view.findViewById<View>(R.id.btnExportarPDF).setOnClickListener {
            // TODO: generar y compartir PDF
        }
        view.findViewById<View>(R.id.btnExportarCSV).setOnClickListener {
            // TODO: generar y compartir CSV
        }
        view.findViewById<View>(R.id.btnExportar).setOnClickListener {
            // TODO: menú de exportación rápida
        }
    }
}

// ─────────────────────────────────────────────
// VISTA: GRÁFICA DE BARRAS
// ─────────────────────────────────────────────

class BarChartView @JvmOverloads constructor(
    context: android.content.Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var datos: List<DatosDia> = emptyList()
    private var seleccionado = 4
    private var clickListener: ((Int) -> Unit)? = null

    private val paintBarra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E1E3A")
    }
    private val paintActiva = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7C3AED")
    }
    private val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8888AA")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }
    private val paintValor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A78BFA")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    fun setDatos(d: List<DatosDia>, sel: Int) {
        datos = d; seleccionado = sel; invalidate()
    }

    fun setOnBarClickListener(l: (Int) -> Unit) { clickListener = l }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.action == android.view.MotionEvent.ACTION_UP && datos.isNotEmpty()) {
            val barW = width.toFloat() / datos.size
            val idx  = (event.x / barW).toInt().coerceIn(0, datos.lastIndex)
            clickListener?.invoke(idx)
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (datos.isEmpty()) return

        val barW      = width.toFloat() / datos.size
        val maxH      = height.toFloat() * 0.70f
        val baseY     = height.toFloat() * 0.82f
        val labelY    = height.toFloat()
        val barPad    = barW * 0.18f
        val maxVal    = datos.maxOf { it.porcentaje }.toFloat()

        datos.forEachIndexed { i, d ->
            val barH   = (d.porcentaje / maxVal) * maxH
            val left   = i * barW + barPad
            val right  = (i + 1) * barW - barPad
            val top    = baseY - barH
            val cx     = i * barW + barW / 2f

            val paint = if (i == seleccionado) paintActiva else paintBarra
            canvas.drawRoundRect(RectF(left, top, right, baseY), 8f, 8f, paint)

            if (i == seleccionado) {
                canvas.drawText("${d.porcentaje}%", cx, top - 8f, paintValor)
            }
            canvas.drawText(d.nombre, cx, labelY, paintTexto)
        }
    }
}

// ─────────────────────────────────────────────
// VISTA: GRÁFICA DE LÍNEA
// ─────────────────────────────────────────────

class LineChartView @JvmOverloads constructor(
    context: android.content.Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val valores = listOf(55, 58, 60, 63, 68, 73)
    private val meses   = listOf("Sep", "Oct", "Nov", "Dic", "Ene", "Feb")

    private val paintLinea = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = Color.parseColor("#7C3AED")
        strokeWidth = 4f
        style       = Paint.Style.STROKE
    }
    private val paintRelleno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A1A5A")
        style = Paint.Style.FILL
    }
    private val paintPunto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7C3AED")
    }
    private val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.parseColor("#555577")
        textSize  = 22f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padH  = 20f
        val padV  = 20f
        val chartW = width.toFloat() - padH * 2
        val chartH = height.toFloat() - padV * 3
        val maxV  = valores.max().toFloat()
        val minV  = valores.min().toFloat()
        val rango = maxV - minV

        val puntos = valores.mapIndexed { i, v ->
            val x = padH + i * (chartW / (valores.size - 1))
            val y = padV + chartH - ((v - minV) / rango) * chartH
            Pair(x, y)
        }

        // Relleno
        val path = Path()
        path.moveTo(puntos.first().first, height.toFloat() - padV)
        puntos.forEach { (x, y) -> path.lineTo(x, y) }
        path.lineTo(puntos.last().first, height.toFloat() - padV)
        path.close()
        canvas.drawPath(path, paintRelleno)

        // Línea
        val linePath = Path()
        puntos.forEachIndexed { i, (x, y) ->
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        canvas.drawPath(linePath, paintLinea)

        // Punto final
        canvas.drawCircle(puntos.last().first, puntos.last().second, 8f, paintPunto)

        // Labels meses
        meses.forEachIndexed { i, m ->
            val x = padH + i * (chartW / (meses.size - 1))
            canvas.drawText(m, x, height.toFloat(), paintTexto)
        }
    }
}