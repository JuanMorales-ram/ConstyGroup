package com.example.consty_group.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import com.google.android.material.snackbar.Snackbar

/**
 * ConfiguracionSistemaFragment
 * ─────────────────────────────
 * Pantalla de configuración del sistema del panel admin.
 * Archivo nuevo — NO modifica ningún archivo existente.
 *
 * Navegación:
 *   supportFragmentManager.beginTransaction()
 *       .replace(R.id.fragmentContainer, ConfiguracionSistemaFragment())
 *       .addToBackStack(null)
 *       .commit()
 */
class ConfiguracionSistemaFragment : Fragment() {

    // ── Modelos ──
    data class Categoria(
        val nombre: String,
        val emoji: String,
        val colorHex: String,
        val habitos: Int,
        var visible: Boolean
    )

    data class VersionHistorial(
        val numero: String,
        val descripcion: String,
        val fecha: String,
        val tipo: TipoVersion  // ACTUAL, FIX, BREAKING, PARCHE, NORMAL
    )

    enum class TipoVersion { ACTUAL, FIX, BREAKING, PARCHE, NORMAL }

    // ── Datos de ejemplo ──
    private val categorias = mutableListOf(
        Categoria("Salud",        "💚", "#22C55E", 7,  true),
        Categoria("Productividad","💜", "#818CF8", 8,  true),
        Categoria("Bienestar",    "🩵", "#2DD4BF", 6,  true),
        Categoria("Fitness",      "🧡", "#FB923C", 7,  true),
        Categoria("Educación",    "💛", "#FACC15", 3,  false),
        Categoria("Social",       "❤️", "#F87171", 2,  true)
    )

    private val textosCampos = mapOf(
        "saludo"       to Triple("🏠 Saludo principal",      "Texto que ve el usuario al abrir la app",    "¡Hola, {nombre}!"),
        "subtitulo"    to Triple("⚡ Subtítulo motivacional", "",                                            "Construye tu mejor versión."),
        "vacio"        to Triple("📭 Estado vacío",           "Cuando el usuario no tiene hábitos",          "Aún no tienes hábitos. ¡Empieza hoy!"),
        "racha"        to Triple("🔥 Mensaje de racha",       "Mostrado en días de racha activa",            "¡{días} días de racha!"),
        "celebracion"  to Triple("🎉 Mensaje de celebración", "Al completar todos los hábitos del día",     "¡Felicidades! ¡Has completado todos los hábitos de hoy!"),
        "progreso"     to Triple("📊 Intro de progreso",      "Encabezado de pantalla de progreso",         "Tu progreso de hoy"),
        "boton"        to Triple("➕ Botón agregar hábito",   "Texto del botón principal",                  "Agregar Hábito"),
        "confirmacion" to Triple("✅ Confirmación completado","Aviso al marcar un hábito",                  "¡Así se hace! Hábito completado 🎉")
    )

    private val historialVersiones = listOf(
        VersionHistorial("v1.4.2", "Corrección de bugs en animaciones de racha", "15 feb 2026", TipoVersion.ACTUAL),
        VersionHistorial("v1.4.1", "Mejora de rendimiento en lista de hábitos",  "1 feb 2026",  TipoVersion.NORMAL),
        VersionHistorial("v1.4.0", "Nuevo diseño de tarjetas y estadísticas",    "18 ene 2026", TipoVersion.FIX),
        VersionHistorial("v1.3.5", "Se corrigió login con email",                "5 ene 2026",  TipoVersion.PARCHE),
        VersionHistorial("v1.3.0", "Refactorización completa — primera versión estable", "10 nov 2025", TipoVersion.BREAKING)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_configuracion_sistema, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarTabs(view)
        configurarCategorias(view)
        configurarTextos(view)
        configurarSistema(view)
    }

    // ─── TABS ───────────────────────────────────────────
    private fun configurarTabs(view: View) {
        val paneles = listOf(
            view.findViewById<View>(R.id.panelCategorias),
            view.findViewById<View>(R.id.panelTextos),
            view.findViewById<View>(R.id.panelSistema)
        )
        val tabs = listOf(
            view.findViewById<LinearLayout>(R.id.tabCategorias),
            view.findViewById<LinearLayout>(R.id.tabTextos),
            view.findViewById<LinearLayout>(R.id.tabSistema)
        )

        fun activar(idx: Int) {
            paneles.forEachIndexed { i, p -> p.visibility = if (i == idx) View.VISIBLE else View.GONE }
            tabs.forEachIndexed { i, t ->
                t.setBackgroundResource(if (i == idx) R.drawable.bg_tab_active else 0)
                val tv = t.getChildAt(1) as? TextView
                tv?.setTextColor(if (i == idx) Color.WHITE else Color.parseColor("#8888AA"))
            }
        }

        tabs.forEachIndexed { i, tab -> tab.setOnClickListener { activar(i) } }
        activar(0)
    }

    // ─── CATEGORÍAS ─────────────────────────────────────
    private fun configurarCategorias(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.llCategorias)
        container.removeAllViews()

        categorias.forEachIndexed { index, cat ->
            val item = layoutInflater.inflate(R.layout.item_categoria_config, container, false)

            item.findViewById<View>(R.id.viewCatColor)
                .setBackgroundColor(Color.parseColor(cat.colorHex))

            item.findViewById<TextView>(R.id.tvCatNombre).text = "${cat.emoji} ${cat.nombre}"

            val tvMeta = item.findViewById<TextView>(R.id.tvCatMeta)
            tvMeta.text = "${cat.habitos} hábitos · ${if (cat.visible) "Visible" else "Oculto"}"

            val tvTag = item.findViewById<TextView>(R.id.tvCatTag)
            if (cat.visible) {
                tvTag.text = "Visible"
                tvTag.setTextColor(Color.parseColor("#22C55E"))
                tvTag.setBackgroundResource(R.drawable.bg_badge_green_dark)
            } else {
                tvTag.text = "Oculto"
                tvTag.setTextColor(Color.parseColor("#F87171"))
                tvTag.setBackgroundResource(R.drawable.bg_badge_red_dark)
            }

            val sw = item.findViewById<SwitchCompat>(R.id.switchCategoria)
            sw.isChecked = cat.visible
            sw.setOnCheckedChangeListener { _, checked ->
                categorias[index] = cat.copy(visible = checked)
                configurarCategorias(view) // Refrescar
            }

            item.findViewById<ImageView>(R.id.btnEditarCategoria).setOnClickListener {
                // TODO: abrir diálogo para editar categoría
                Snackbar.make(view, "Editar categoría: ${cat.nombre}", Snackbar.LENGTH_SHORT).show()
            }
            item.findViewById<ImageView>(R.id.btnEliminarCategoria).setOnClickListener {
                // TODO: confirmar y eliminar categoría
                Snackbar.make(view, "¿Eliminar ${cat.nombre}?", Snackbar.LENGTH_SHORT).show()
            }

            container.addView(item)
        }

        view.findViewById<TextView>(R.id.btnAgregarCategoria).setOnClickListener {
            // TODO: abrir diálogo para crear nueva categoría
            Snackbar.make(view, "Crear nueva categoría", Snackbar.LENGTH_SHORT).show()
        }
    }

    // ─── TEXTOS ─────────────────────────────────────────
    private fun configurarTextos(view: View) {
        val camposIds = mapOf(
            "saludo"       to R.id.fieldSaludo,
            "subtitulo"    to R.id.fieldSubtitulo,
            "vacio"        to R.id.fieldVacio,
            "racha"        to R.id.fieldRacha,
            "celebracion"  to R.id.fieldCelebracion,
            "progreso"     to R.id.fieldProgreso,
            "boton"        to R.id.fieldBotonHabito,
            "confirmacion" to R.id.fieldConfirmacion
        )

        camposIds.forEach { (clave, viewId) ->
            val campoView = view.findViewById<View>(viewId)
            val info = textosCampos[clave] ?: return@forEach
            campoView.findViewById<TextView>(R.id.tvTextoLabel)?.text = info.first
            campoView.findViewById<TextView>(R.id.tvTextoSub)?.apply {
                text = info.second
                visibility = if (info.second.isEmpty()) View.GONE else View.VISIBLE
            }
            campoView.findViewById<EditText>(R.id.etTextoValor)?.setText(info.third)
        }

        view.findViewById<Button>(R.id.btnGuardarTextos).setOnClickListener {
            // TODO: guardar textos en Firestore / SharedPreferences
            Snackbar.make(view, "✅ Textos guardados correctamente", Snackbar.LENGTH_SHORT).show()
        }
    }

    // ─── SISTEMA ────────────────────────────────────────
    private fun configurarSistema(view: View) {

        // Switches de mantenimiento
        view.findViewById<SwitchCompat>(R.id.switchMantenimiento)
            .setOnCheckedChangeListener { _, on ->
                Snackbar.make(view,
                    if (on) "Modo mantenimiento ACTIVADO" else "Modo mantenimiento desactivado",
                    Snackbar.LENGTH_SHORT).show()
            }

        view.findViewById<SwitchCompat>(R.id.switchForzarActualizacion)
            .setOnCheckedChangeListener { _, on ->
                Snackbar.make(view,
                    if (on) "Actualización forzada ACTIVADA" else "Actualización forzada desactivada",
                    Snackbar.LENGTH_SHORT).show()
            }

        // Versión mínima
        view.findViewById<Button>(R.id.btnAplicarVersion).setOnClickListener {
            val v = view.findViewById<EditText>(R.id.etVersionMinima).text.toString().trim()
            if (v.isNotEmpty()) {
                // TODO: actualizar versión mínima en Firestore
                Snackbar.make(view, "Versión mínima aplicada: $v", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Spinner ambiente
        val spinner = view.findViewById<Spinner>(R.id.spinnerAmbiente)
        val ambientes = listOf("🟢 Producción", "🟡 Staging", "🔴 Desarrollo")
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ambientes)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Botón desplegar
        view.findViewById<Button>(R.id.btnDesplegar).setOnClickListener {
            val version = view.findViewById<EditText>(R.id.etNuevaVersion).text.toString().trim()
            val ambiente = spinner.selectedItem.toString()
            val notas   = view.findViewById<EditText>(R.id.etNotasRelease).text.toString().trim()

            if (version.isEmpty()) {
                Snackbar.make(view, "Indica la versión a desplegar", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO: llamar a tu pipeline de CI/CD o Firestore
            Snackbar.make(view, "🚀 Desplegando $version en $ambiente...", Snackbar.LENGTH_LONG).show()
        }

        // Historial de versiones
        val llHistorial = view.findViewById<LinearLayout>(R.id.llHistorialVersiones)
        llHistorial.removeAllViews()

        historialVersiones.forEachIndexed { i, ver ->
            val item = layoutInflater.inflate(R.layout.item_version_historial, llHistorial, false)
            item.findViewById<TextView>(R.id.tvVerNumero).text = ver.numero
            item.findViewById<TextView>(R.id.tvVerDesc).text   = ver.descripcion
            item.findViewById<TextView>(R.id.tvVerFecha).text  = ver.fecha

            val tvTag = item.findViewById<TextView>(R.id.tvVerTag)
            when (ver.tipo) {
                TipoVersion.ACTUAL -> {
                    tvTag.text = "Actual"; tvTag.visibility = View.VISIBLE
                    tvTag.setTextColor(Color.parseColor("#22C55E"))
                    tvTag.setBackgroundResource(R.drawable.bg_badge_green_dark)
                }
                TipoVersion.FIX -> {
                    tvTag.text = "Fix"; tvTag.visibility = View.VISIBLE
                    tvTag.setTextColor(Color.parseColor("#60A5FA"))
                    tvTag.setBackgroundResource(R.drawable.bg_badge_blue_dark)
                }
                TipoVersion.BREAKING -> {
                    tvTag.text = "Breaking"; tvTag.visibility = View.VISIBLE
                    tvTag.setTextColor(Color.parseColor("#F87171"))
                    tvTag.setBackgroundResource(R.drawable.bg_badge_red_dark)
                }
                TipoVersion.PARCHE -> {
                    tvTag.text = "Parche"; tvTag.visibility = View.VISIBLE
                    tvTag.setTextColor(Color.parseColor("#FB923C"))
                    tvTag.setBackgroundResource(R.drawable.bg_badge_orange_dark)
                }
                TipoVersion.NORMAL -> tvTag.visibility = View.GONE
            }

            // Sin divisor en el último item
            if (i == historialVersiones.lastIndex) {
                item.findViewById<View>(R.id.dividerVersion)?.visibility = View.GONE
            }

            llHistorial.addView(item)
        }
    }
}