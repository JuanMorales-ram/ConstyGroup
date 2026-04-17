package com.example.consty_group.main.abito

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R



class AbitoAdapter(private val data: MutableList<AbitoItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

        when (item) {

            is AbitoSImple -> {
                val h = holder as SimpleViewHolder

                h.nombre.text = item.nombre
                h.dias.text = "${item.dias} días"
                h.icono.setImageResource(item.icono)

                h.itemView.setOnClickListener {
                    data[position] = AbitoComplejo(
                        item.nombre,
                        item.dias,
                        item.color,
                        item.icono
                    )
                    notifyItemChanged(position)
                }
            }

            is AbitoComplejo -> {
                val h = holder as ComplejoViewHolder

                h.nombre.text = item.nombre
                h.dias.text = "${item.dias} días"
                h.icono.setImageResource(item.icono)

                h.btnCompletar.setOnClickListener {
                    data[position] = AbitoSImple(
                        item.nombre,
                        item.dias + 1,
                        item.color,
                        item.icono
                    )
                    notifyItemChanged(position)
                }

                h.btnCompletarSin.setOnClickListener {
                    data[position] = AbitoSImple(
                        item.nombre,
                        item.dias,
                        item.color,
                        item.icono
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
    }

    class ComplejoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val dias: TextView = view.findViewById(R.id.txtDias)
        val icono: ImageView = view.findViewById(R.id.iconoHabito)

        val btnCompletar: Button = view.findViewById(R.id.btnCompletar)
        val btnCompletarSin: Button = view.findViewById(R.id.btnCompletarSin)
    }
}