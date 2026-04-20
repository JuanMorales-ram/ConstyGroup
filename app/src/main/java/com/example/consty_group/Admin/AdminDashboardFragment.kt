package com.example.consty_group.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AdminDashboardFragment
 * ──────────────────────
 * Pantalla principal del panel de administrador.
 * Archivo nuevo — NO modifica ningún archivo existente del proyecto.
 *
 * Para navegar aquí desde MainActivity o desde un botón de admin,
 * usa:
 *   supportFragmentManager.beginTransaction()
 *       .replace(R.id.fragmentContainer, AdminDashboardFragment())
 *       .addToBackStack(null)
 *       .commit()
 */
class AdminDashboardFragment : Fragment() {

    // ── Datos de ejemplo (reemplazar con tu fuente de datos real) ──
    private val totalUsuarios = 1284
    private val tasaCompletacion = 73
    private val activosHoy = 347
    private val activosSemana = 892
    private val activosMes = 1100

    data class HabitoPopular(
        val rank: String,
        val emoji: String,
        val nombre: String,
        val usuarios: Int,
        val porcentaje: Int,
        val colorHex: String
    )

    private val habitosPopulares = listOf(
        HabitoPopular("⚡", "💧", "Beber agua",  1024, 90, "#2DD4BF"),
        HabitoPopular("#2", "🏋️", "Ejercicio",   891,  76, "#F87171"),
        HabitoPopular("#3", "🧘", "Meditación",  743,  82, "#A78BFA"),
        HabitoPopular("#4", "📖", "Leer 30 min", 612,  61, "#60A5FA"),
        HabitoPopular("#5", "🌙", "Dormir 8h",   528,  54, "#FACC15")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarFecha(view)
        configurarTarjetas(view)
        configurarDonut(view)
        configurarProgresoBars(view)
        configurarHabitosPopulares(view)
        configurarFooter(view)
    }

    private fun configurarFecha(view: View) {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM · yyyy", Locale("es", "ES"))
        val fecha = sdf.format(Date()).replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.tvFechaAdmin).text = fecha
    }

    private fun configurarTarjetas(view: View) {
        view.findViewById<TextView>(R.id.tvTotalUsuarios).text =
            String.format("%,d", totalUsuarios).replace(',', '.')

        view.findViewById<TextView>(R.id.tvTasaCompletacion).text = "$tasaCompletacion%"

        view.findViewById<TextView>(R.id.tvActivosHoy).text = "$activosHoy"
        val pctHoy = (activosHoy * 100) / totalUsuarios
        view.findViewById<TextView>(R.id.tvPctActivosHoy).text = "$pctHoy% del total"

        view.findViewById<TextView>(R.id.tvActivosSemana).text = "$activosSemana"
        val pctSem = (activosSemana * 100) / totalUsuarios
        view.findViewById<TextView>(R.id.tvPctActivosSemana).text = "$pctSem% del total"

        view.findViewById<TextView>(R.id.tvCompletacionActual).text = "$tasaCompletacion%"
    }

    private fun configurarDonut(view: View) {
        view.findViewById<DonutChartView>(R.id.donutChart).apply {
            setProgress(tasaCompletacion, 100)
            setColors(
                trackColor  = Color.parseColor("#1E1E3A"),
                progressColor = Color.parseColor("#2DD4BF")
            )
            setCenterText("$tasaCompletacion%")
        }
    }

    private fun configurarProgresoBars(view: View) {
        val pctHoy  = (activosHoy   * 100) / totalUsuarios
        val pctSem  = (activosSemana * 100) / totalUsuarios
        val pctMes  = (activosMes   * 100) / totalUsuarios

        view.findViewById<TextView>(R.id.tvActivosHoyLabel).text =
            "$activosHoy / ${String.format("%,d", totalUsuarios).replace(',', '.')}"
        view.findViewById<ProgressBar>(R.id.pbActivosHoy).progress = pctHoy

        view.findViewById<TextView>(R.id.tvActivosSemanaLabel).text =
            "$activosSemana / ${String.format("%,d", totalUsuarios).replace(',', '.')}"
        view.findViewById<ProgressBar>(R.id.pbActivosSemana).progress = pctSem

        view.findViewById<TextView>(R.id.tvActivosMesLabel).text =
            "${String.format("%,d", activosMes).replace(',', '.')} / ${String.format("%,d", totalUsuarios).replace(',', '.')}"
        view.findViewById<ProgressBar>(R.id.pbActivosMes).progress = pctMes
    }

    private fun configurarHabitosPopulares(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.llHabitosPopulares)
        container.removeAllViews()


    }

    private fun configurarFooter(view: View) {
        view.findViewById<TextView>(R.id.tvUltimaActualizacion).text =
            "Última actualización: hace 2 minutos · HabitTracker Admin v1.0"
    }
}
