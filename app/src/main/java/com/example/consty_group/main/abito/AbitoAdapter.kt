package com.example.consty_group.main.abito

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R

class AbitoAdapter(
    private val data: MutableList<AbitoItem>,
    private val onHabitChanged: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TIPO_SIMPLE = 0
        const val TIPO_COMPLEJO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is AbitoSImple -> TIPO_SIMPLE
            is AbitoComplejo -> TIPO_COMPLEJO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_SIMPLE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_abito_simple, parent, false)
            SimpleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_abito_complejo, parent, false)
            ComplejoViewHolder(view)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val colorStateList = ColorStateList.valueOf(item.color)

        when (item) {
            is AbitoSImple -> {
                val h = holder as SimpleViewHolder
                h.nombre.text = item.nombre
                h.dias.text = "${item.dias} días"
                h.icono.setImageResource(item.icono)
                
                // Aplicar color de la estructura
                h.icono.imageTintList = colorStateList
                h.iconCamara.imageTintList = colorStateList
                h.iconMapa.imageTintList = colorStateList
                h.check.backgroundTintList = colorStateList

                h.check.setOnCheckedChangeListener(null)
                h.check.isChecked = item.completadoHoy

                h.check.setOnCheckedChangeListener { _, isChecked ->
                    item.completadoHoy = isChecked
                    onHabitChanged()
                }

                h.itemView.setOnClickListener {
                    data[position] = AbitoComplejo(
                        item.nombre,
                        item.dias,
                        item.color,
                        item.icono,
                        item.completadoHoy
                    )
                    notifyItemChanged(position)
                }
            }

            is AbitoComplejo -> {
                val h = holder as ComplejoViewHolder
                h.nombre.text = item.nombre
                h.dias.text = "${item.dias} Dias"
                h.icono.setImageResource(item.icono)

                // Aplicar color de la estructura
                h.icono.imageTintList = colorStateList
                h.btnFotoIcon.imageTintList = colorStateList
                h.btnUbicacionIcon.imageTintList = colorStateList
                h.btnCompletar.backgroundTintList = colorStateList

                h.btnCompletar.setOnClickListener {
                    data[position] = AbitoSImple(
                        item.nombre,
                        item.dias + 1,
                        item.color,
                        item.icono,
                        true
                    )
                    onHabitChanged()
                    notifyItemChanged(position)
                }

                h.btnCompletarSin.setOnClickListener {
                    data[position] = AbitoSImple(
                        item.nombre,
                        item.dias,
                        item.color,
                        item.icono,
                        item.completadoHoy
                    )
                    notifyItemChanged(position)
                }
            }
        }
    }

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val dias: TextView = view.findViewById(R.id.txtDias)
        val icono: ImageView = view.findViewById(R.id.iconoHabito)
        val iconCamara: ImageView = view.findViewById(R.id.iconCamaraSimple)
        val iconMapa: ImageView = view.findViewById(R.id.iconMapaSimple)
        val check: CheckBox = view.findViewById(R.id.check)
    }

    class ComplejoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val dias: TextView = view.findViewById(R.id.txtDias)
        val icono: ImageView = view.findViewById(R.id.iconoHabito)
        val btnFotoIcon: ImageView = view.findViewById(R.id.btnFotoIcon)
        val btnUbicacionIcon: ImageView = view.findViewById(R.id.btnUbicacionIcon)
        val btnCompletar: Button = view.findViewById(R.id.btnCompletar)
        val btnCompletarSin: Button = view.findViewById(R.id.btnCompletarSin)
    }
}