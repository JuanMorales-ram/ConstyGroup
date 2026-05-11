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
import com.example.consty_group.data.Habito

class RachaHabitoAdapter(
    private val listaHabitos: List<Pair<Habito, Int>> // Recibe el Hábito + el número de racha
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

    override fun getItemCount(): Int = listaHabitos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (habito, diasDeRacha) = listaHabitos[position]
        val context = holder.itemView.context

        holder.tvDias.text = "$diasDeRacha Dias"
        holder.tvNombre.text = habito.nombre
        holder.ivIcono.setImageResource(habito.icono_res_id)

        // Lógica de colores basada en la intensidad de la racha
        val colorRes = when {
            diasDeRacha >= 21 -> R.color.ColorRojo        // Racha legendaria
            diasDeRacha >= 7  -> R.color.BorderTercerLogro // Racha semanal
            diasDeRacha >= 3  -> R.color.BorderPrimerLogro // Racha inicial
            else              -> R.color.CardPerfil        // Recién empezando
        }

        val colorInt = ContextCompat.getColor(context, colorRes)

        // Borde dinámico según la racha
        val borderRes = when {
            diasDeRacha >= 21 -> R.drawable.border_racha_rojo
            diasDeRacha >= 7  -> R.drawable.borde_racha_celeste
            diasDeRacha >= 3  -> R.drawable.borde_racha_amarillo
            else              -> R.drawable.borde_racha_azul
        }
        holder.container.setBackgroundResource(borderRes)

        // Aplicar el color del icono
        holder.ivIcono.imageTintList = ColorStateList.valueOf(colorInt)

        // Fondo semi-transparente del icono
        val colorFondoSemi = Color.argb(38, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        holder.layoutIcono.backgroundTintList = ColorStateList.valueOf(colorFondoSemi)

        // Check visual de si hoy ya se cumplió
        if (habito.completadoHoy) {
            holder.ivCheck.imageTintList = null // Color original del recurso (verde/blanco)
        } else {
            val colorGris = ContextCompat.getColor(context, R.color.backgroundColo)
            holder.ivCheck.imageTintList = ColorStateList.valueOf(colorGris)
        }
    }
}
