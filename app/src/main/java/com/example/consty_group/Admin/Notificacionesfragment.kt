package com.example.consty_group.admin

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
    data class Plantilla(val tituloRes: Int, val emoji: String, val mensajeRes: Int)

    private val plantillasMap = mapOf(
        "motivacion"  to Plantilla(R.string.motivaci_n_ma_anera,  "🌅", R.string.buenos_d_as_hoy_es_un_gran_d_a_para),
        "recordatorio" to Plantilla(R.string.recordatorio_h_bito, "⏰", R.string.no_olvides_completar_tu_h_bito_de),
        "celebracion" to Plantilla(R.string.celebraci_n_racha,    "🔥", R.string.incre_ble_llevas_x_d_as_seguidos),
        "finsemana"   to Plantilla(R.string.fin_de_semana,        "🎉", R.string.es_fin_de_semana)
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
    ): View? = inflater.inflate(R.layout.fragment_notificaciones, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarTabs(view)
        configurarPlantillas(view)
        configurarEmojiSelector(view)
        configurarCampos(view)
        configurarAudiencias(view)
        configurarSwitch(view)
        configurarBotonEnviar(view)
        actualizarPreview(view)
        actualizarBoton(view)
    }

    private fun configurarTabs(view: View) {
        val tabs = listOf(
            view.findViewById<LinearLayout>(R.id.tabEnviar),
            view.findViewById<LinearLayout>(R.id.tabMensajes),
            view.findViewById<LinearLayout>(R.id.tabProgramar),
            view.findViewById<LinearLayout>(R.id.tabHistorial)
        )
        tabs.forEach { tab ->
            tab.setOnClickListener {
                tabs.forEach { t -> t.setBackgroundResource(0) }
            }
        }
    }

    private fun configurarPlantillas(view: View) {
        mapOf(
            R.id.tmplMotivacion   to "motivacion",
            R.id.tmplRecordatorio to "recordatorio",
            R.id.tmplCelebracion  to "celebracion",
            R.id.tmplFinSemana    to "finsemana"
        ).forEach { (viewId, clave) ->
            view.findViewById<View>(viewId).setOnClickListener {
                val p = plantillasMap[clave] ?: return@setOnClickListener
                view.findViewById<EditText>(R.id.etTituloNotif).setText(getString(p.tituloRes))
                view.findViewById<EditText>(R.id.etMensajeNotif).setText(getString(p.mensajeRes))
                emojiSeleccionado = p.emoji
                view.findViewById<EmojiSelectorView>(R.id.emojiSelector).setSelectedEmoji(p.emoji)
                actualizarPreview(view)
            }
        }
    }

    private fun configurarEmojiSelector(view: View) {
        view.findViewById<EmojiSelectorView>(R.id.emojiSelector)
            .setOnEmojiSelectedListener { emoji ->
                emojiSeleccionado = emoji
                actualizarPreview(view)
            }
    }

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
                view.findViewById<View>(viewId).setBackgroundResource(R.drawable.bg_card_dark_selected)

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

    private fun configurarSwitch(view: View) {
        view.findViewById<SwitchCompat>(R.id.switchProgramar).setOnCheckedChangeListener { _, _ -> }
    }

    private fun configurarBotonEnviar(view: View) {
        view.findViewById<Button>(R.id.btnEnviarNotif).setOnClickListener {
            val titulo  = view.findViewById<EditText>(R.id.etTituloNotif).text.toString().trim()
            val mensaje = view.findViewById<EditText>(R.id.etMensajeNotif).text.toString().trim()

            if (titulo.isEmpty() || mensaje.isEmpty()) {
                Snackbar.make(view, getString(R.string.error_campos_notif), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Snackbar.make(view, getString(R.string.notif_enviada_format, contadorAudiencia), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun actualizarPreview(view: View) {
        val titulo  = view.findViewById<EditText>(R.id.etTituloNotif).text.toString()
        val mensaje = view.findViewById<EditText>(R.id.etMensajeNotif).text.toString()
        view.findViewById<TextView>(R.id.tvPreviewTitle).text = titulo.ifEmpty { getString(R.string.t_tulo_de_la_notificaci_n) }
        view.findViewById<TextView>(R.id.tvPreviewBody).text = mensaje.ifEmpty { getString(R.string.cuerpo_del_mensaje) }
        view.findViewById<TextView>(R.id.tvPreviewIcon).text = emojiSeleccionado
    }

    private fun actualizarBoton(view: View) {
        val formatCount = String.format("%,d", contadorAudiencia).replace(',', '.')
        view.findViewById<Button>(R.id.btnEnviarNotif).text = getString(R.string.enviar_ahora_format, formatCount)
    }
}