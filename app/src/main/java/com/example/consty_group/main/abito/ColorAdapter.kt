package com.example.consty_group.main.abito

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R

class ColorAdapter(
    private val colores: List<String>,
    private val onColorClick: (String) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private var seleccionado = 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vColor: View = view.findViewById(R.id.vItemColor)
        val vBorde: View = view.findViewById(R.id.vBordeColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_selector, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = colores.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = Color.parseColor(colores[position])

        // Círculo de color
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        holder.vColor.background = drawable

        // Borde blanco si está seleccionado
        holder.vBorde.visibility = if (position == seleccionado) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val prev = seleccionado
            seleccionado = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            onColorClick(colores[position])
        }
    }
}