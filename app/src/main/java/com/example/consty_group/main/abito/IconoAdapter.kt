package com.example.consty_group.main.abito

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R

class IconoAdapter(
    private val iconos: List<Int>,
    private val onIconoClick: (Int) -> Unit
) : RecyclerView.Adapter<IconoAdapter.ViewHolder>() {

    private var seleccionado = 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcono: ImageView = view.findViewById(R.id.ivItemIcono)
        val vSeleccion: View = view.findViewById(R.id.vSeleccionIcono)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icono_selector, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = iconos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivIcono.setImageResource(iconos[position])
        holder.vSeleccion.visibility = if (position == seleccionado) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val prev = seleccionado
            seleccionado = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            onIconoClick(iconos[position])
        }
    }
}