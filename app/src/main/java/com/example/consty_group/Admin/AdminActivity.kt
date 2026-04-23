package com.example.consty_group.admin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.consty_group.R

/**
 * AdminActivity
 * ─────────────
 * Actividad principal del módulo administrativo.
 * Reemplaza al antiguo AdminContainerFragment para ofrecer una navegación
 * a pantalla completa e independiente del flujo principal de la app.
 */
class AdminActivity : AppCompatActivity() {

    private var tabActual = -1
    private val fragments = arrayOfNulls<Fragment>(4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        configurarTabs()
        mostrarTab(0) // Iniciar siempre en el Dashboard
    }

    private fun configurarTabs() {
        findViewById<LinearLayout>(R.id.tabDashboard).setOnClickListener      { mostrarTab(0) }
        findViewById<LinearLayout>(R.id.tabUsuarios).setOnClickListener       { mostrarTab(1) }
        findViewById<LinearLayout>(R.id.tabEstadisticas).setOnClickListener   { mostrarTab(2) }
        findViewById<LinearLayout>(R.id.tabNotificaciones).setOnClickListener { mostrarTab(3) }
    }

    private fun mostrarTab(index: Int) {
        if (tabActual == index) return
        tabActual = index

        val fragment = when (index) {
            0 -> fragments[0] ?: AdminDashboardFragment().also    { fragments[0] = it }
            1 -> fragments[1] ?: GestionUsuariosFragment().also   { fragments[1] = it }
            2 -> fragments[2] ?: EstadisticasFragment().also      { fragments[2] = it }
            3 -> fragments[3] ?: NotificacionesFragment().also    { fragments[3] = it }
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()

        actualizarTabBar(index)
    }

    private fun actualizarTabBar(activo: Int) {
        val tabs = listOf(
            TabViews(findViewById(R.id.tabLabelDashboard), findViewById(R.id.tabDotDashboard)),
            TabViews(findViewById(R.id.tabLabelUsuarios), findViewById(R.id.tabDotUsuarios)),
            TabViews(findViewById(R.id.tabLabelEstadisticas), findViewById(R.id.tabDotEstadisticas)),
            TabViews(findViewById(R.id.tabLabelNotificaciones), findViewById(R.id.tabDotNotificaciones))
        )

        val colorActivo = ContextCompat.getColor(this, R.color.colorMeta)
        val colorInactivo = ContextCompat.getColor(this, R.color.tabUnselected)

        tabs.forEachIndexed { i, t ->
            if (i == activo) {
                t.label.setTextColor(colorActivo)
                t.label.setTypeface(null, android.graphics.Typeface.BOLD)
                t.dot.visibility = View.VISIBLE
            } else {
                t.label.setTextColor(colorInactivo)
                t.label.setTypeface(null, android.graphics.Typeface.NORMAL)
                t.dot.visibility = View.INVISIBLE
            }
        }
    }

    private data class TabViews(val label: TextView, val dot: View)
}