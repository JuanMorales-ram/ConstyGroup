package com.example.consty_group.main.abito

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R

// Definimos qué colores puede tener el borde
enum class ColorRacha { AZUL, AMARILLO, ROJO, CELESTE }

// Molde del hábito
data class RachaHabito(
    val nombre: String,
    val icono: Int,
    val diasRacha: Int,
    val colorBorde: ColorRacha,
    val completadoHoy: Boolean = false
)

class RachaHabitoAdapter(
    private val lista: List<RachaHabito>
) : RecyclerView.Adapter<RachaHabitoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDias: TextView = view.findViewById(R.id.tvDiasRacha)
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoHabito)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreHabito)
        val ivCheck: ImageView = view.findViewById(R.id.ivCheckRacha)
        val container: View = view.findViewById(R.id.containerRacha)
        val layoutIcono: View = view.findViewById(R.id.layoutIconoHabito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_racha_habito, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habito = lista[position]
        val context = holder.itemView.context

        holder.tvDias.text = "${habito.diasRacha} Dias"
        holder.tvNombre.text = habito.nombre
        holder.ivIcono.setImageResource(habito.icono)

        // CORRECCIÓN: Obtener color usando ContextCompat
        val colorInt = when (habito.colorBorde) {
            ColorRacha.AZUL     -> ContextCompat.getColor(context, R.color.CardPerfil)
            ColorRacha.AMARILLO -> ContextCompat.getColor(context, R.color.BorderPrimerLogro)
            ColorRacha.ROJO     -> ContextCompat.getColor(context, R.color.ColorRojo)
            ColorRacha.CELESTE  -> ContextCompat.getColor(context, R.color.BorderTercerLogro)
        }

        // Borde
        val borderRes = when (habito.colorBorde) {
            ColorRacha.AZUL     -> R.drawable.borde_racha_azul
            ColorRacha.AMARILLO -> R.drawable.borde_racha_amarillo
            ColorRacha.ROJO     -> R.drawable.border_racha_rojo
            ColorRacha.CELESTE  -> R.drawable.borde_racha_celeste
        }
        holder.container.setBackgroundResource(borderRes)

        // Tinte de icono
        holder.ivIcono.imageTintList = ColorStateList.valueOf(colorInt)

        // Fondo semi-transparente
        val colorFondoSemi = Color.argb(38, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        holder.layoutIcono.backgroundTintList = ColorStateList.valueOf(colorFondoSemi)

        // Check (Gris si no está completado)
        if (habito.completadoHoy) {
            holder.ivCheck.imageTintList = null 
        } else {
            // Asegúrate de que "grisOscuro" exista en colors.xml, si no, usa un color existente
            val colorGris = ContextCompat.getColor(context, R.color.backgroundColo) 
            holder.ivCheck.imageTintList = ColorStateList.valueOf(colorGris)
        }
    }
}
