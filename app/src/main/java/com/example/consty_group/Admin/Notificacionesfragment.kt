package com.example.consty_group.admin

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import com.google.android.material.snackbar.Snackbar

/**
 * NotificacionesFragment
 * ──────────────────────
 * Pantalla de notificaciones y comunicados del panel admin.
 */
class NotificacionesFragment : Fragment() {

    // ── Estado ──
    private var emojiSeleccionado = "🔔"
    private var audienciaSeleccionada = "todos"
    private var contadorAudiencia = 1284

    // ── Plantillas ──
    data class Plantilla(val titulo: String, val emoji: String, val mensaje: String)

    private val plantillas = mapOf(
        "motivacion"  to Plantilla("Motivación mañanera",  "🌅",
            "¡Buenos días! Hoy es un gran día para cumplir tus hábitos. ¡Tú puedes!"),
        "recordatorio" to Plantilla("Recordatorio hábito", "⏰",
            "No olvides completar tu hábito de hoy. ¡Cada día cuenta!"),
        "celebracion" to Plantilla("Celebración racha",    "🔥",
            "¡Increíble! Llevas X días seguidos cumpliendo tus hábitos. ¡Sigue así!"),
        "finsemana"   to Plantilla("Fin de semana",        "🎉",
            "¡Es fin de semana! Mantén tus hábitos y cierra la semana con éxito.")
    )

    // ── Audiencias ──
    data class Audiencia(val id: String, val cantidad: Int)

    private val audiencias = mapOf(
        "todos"     to Audiencia("todos",     1284),
        "activos"   to Audiencia("activos",    892),
        "inactivos" to Audiencia("inactivos",  392),
        "pro"       to Audiencia("pro",        347),
        "riesgo"    to Audiencia("riesgo",     221)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.Fragment_notificaciones, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarTabs(view)
        configurarPlantillas(view)
        configurarEmojiSelector(view)   // ← NUEVO: conecta EmojiSelectorView
        configurarCampos(view)
        configurarAudiencias(view)
        configurarSwitch(view)
        configurarBotonEnviar(view)
    }

    // ── Tabs ──
    private fun configurarTabs(view: View) {
        val tabs = listOf(
            view.findViewById<LinearLayout>(R.id.tabEnviar),
            view.findViewById<LinearLayout>(R.id.tabMensajes),
            view.findViewById<LinearLayout>(R.id.tabProgramar),
            view.findViewById<LinearLayout>(R.id.tabHistorial)
        )
        tabs.forEachIndexed { _, tab ->
            tab.setOnClickListener {
                tabs.forEach { t -> t.setBackgroundResource(0) }
                tab.setBackgroundResource(R.drawable.bg_tab_active)
                // TODO: mostrar contenido del tab seleccionado
            }
        }
    }

    // ── Plantillas ──
    private fun configurarPlantillas(view: View) {
        mapOf(
            R.id.tmplMotivacion   to "motivacion",
            R.id.tmplRecordatorio to "recordatorio",
            R.id.tmplCelebracion  to "celebracion",
            R.id.tmplFinSemana    to "finsemana"
        ).forEach { (viewId, clave) ->
            view.findViewById<View>(viewId).setOnClickListener {
                val p = plantillas[clave] ?: return@setOnClickListener
                view.findViewById<EditText>(R.id.etTituloNotif).setText(p.titulo)
                view.findViewById<EditText>(R.id.etMensajeNotif).setText(p.mensaje)
                // Actualizar emoji selector visualmente y en estado
                emojiSeleccionado = p.emoji
                view.findViewById<EmojiSelectorView>(R.id.emojiSelector)
                    .setSelectedEmoji(p.emoji)
                actualizarPreview(view)
            }
        }
    }

    // ── NUEVO: conectar EmojiSelectorView ──
    private fun configurarEmojiSelector(view: View) {
        view.findViewById<EmojiSelectorView>(R.id.emojiSelector)
            .setOnEmojiSelectedListener { emoji ->
                emojiSeleccionado = emoji
                actualizarPreview(view)
            }
    }

    // ── Campos título y mensaje ──
    private fun configurarCampos(view: View) {
        val etTitulo  = view.findViewById<EditText>(R.id.etTituloNotif)
        val etMensaje = view.findViewById<EditText>(R.id.etMensajeNotif)
        val tvCount   = view.findViewById<TextView>(R.id.tvCharCount)
        val tvCountB  = view.findViewById<TextView>(R.id.tvCharCountBottom)

        etTitulo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                actualizarPreview(view)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etMensaje.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val len = s?.length ?: 0
                tvCount.text  = " ($len/120)"
                tvCountB.text = "$len / 120 caracteres"
                actualizarPreview(view)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ── Audiencias ──
    private fun configurarAudiencias(view: View) {
        val grupos = mapOf(
            R.id.audTodos     to "todos",
            R.id.audActivos   to "activos",
            R.id.audInactivos to "inactivos",
            R.id.audPro       to "pro",
            R.id.audRiesgo    to "riesgo"
        )
        val radios = mapOf(
            "todos"     to R.id.radioTodos,
            "activos"   to R.id.radioActivos,
            "inactivos" to R.id.radioInactivos,
            "pro"       to R.id.radioPro,
            "riesgo"    to R.id.radioRiesgo
        )

        grupos.forEach { (viewId, clave) ->
            view.findViewById<View>(viewId).setOnClickListener {
                audienciaSeleccionada = clave
                contadorAudiencia     = audiencias[clave]?.cantidad ?: 0

                grupos.keys.forEach { id ->
                    view.findViewById<View>(id).setBackgroundResource(R.drawable.bg_card_dark)
                }
                view.findViewById<View>(viewId)
                    .setBackgroundResource(R.drawable.bg_card_dark_selected)

                radios.values.forEach { rid ->
                    view.findViewById<ImageView>(rid).setImageResource(R.drawable.ic_radio_off)
                }
                radios[clave]?.let { rid ->
                    view.findViewById<ImageView>(rid).setImageResource(R.drawable.ic_radio_on)
                }

                actualizarBoton(view)
            }
        }
    }

    // ── Switch programar ──
    private fun configurarSwitch(view: View) {
        view.findViewById<SwitchCompat>(R.id.switchProgramar)
            .setOnCheckedChangeListener { _, _ ->
                // TODO: mostrar selector de fecha/hora
            }
    }

    // ── Botón enviar ──
    private fun configurarBotonEnviar(view: View) {
        view.findViewById<Button>(R.id.btnEnviarNotif).setOnClickListener {
            val titulo  = view.findViewById<EditText>(R.id.etTituloNotif).text.toString().trim()
            val mensaje = view.findViewById<EditText>(R.id.etMensajeNotif).text.toString().trim()

            if (titulo.isEmpty() || mensaje.isEmpty()) {
                Snackbar.make(view, "Completa el título y el mensaje", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO: llamar a tu servicio de envío (Firebase FCM, etc.)
            Snackbar.make(
                view,
                "✅ Notificación enviada a $contadorAudiencia usuarios",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // ── Helpers ──
    private fun actualizarPreview(view: View) {
        val titulo  = view.findViewById<EditText>(R.id.etTituloNotif).text.toString()
        val mensaje = view.findViewById<EditText>(R.id.etMensajeNotif).text.toString()
        view.findViewById<TextView>(R.id.tvPreviewTitle).text =
            titulo.ifEmpty { "Título de la notificación" }
        view.findViewById<TextView>(R.id.tvPreviewBody).text =
            mensaje.ifEmpty { "Cuerpo del mensaje..." }
        view.findViewById<TextView>(R.id.tvPreviewIcon).text = emojiSeleccionado
    }

    private fun actualizarBoton(view: View) {
        view.findViewById<Button>(R.id.btnEnviarNotif).text =
            "🔔 Enviar ahora · ${String.format("%,d", contadorAudiencia).replace(',', '.')} usuarios"
    }
}