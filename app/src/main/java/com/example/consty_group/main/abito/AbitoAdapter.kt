package com.example.consty_group.main.abito

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.graphics.Color
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.consty_group.R
import com.example.consty_group.data.Habito

class AbitoAdapter(
    // Cambiamos AbitoItem por tu modelo de Supabase
    private var data: MutableList<Habito>,
    private val onHabitChanged: (Habito, Boolean) -> Unit, // Pasamos el hábito y si se marcó
    private val onHabitDeleted: (Habito) -> Unit, // Pasamos el hábito a eliminar
    private val onTomarFoto: (Habito) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Función para actualizar la lista cuando descargues los datos de Supabase
    fun updateData(newData: List<Habito>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    companion object {
        const val TIPO_SIMPLE = 0
        const val TIPO_COMPLEJO = 1
    }

    override fun getItemViewType(position: Int): Int {
        //  usamos la propiedad booleana que agregamos
        return if (data[position].esComplejo) TIPO_COMPLEJO else TIPO_SIMPLE
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

        // Convertimos el String Hex (#FFFFFF) a Int de Color
        val colorInt = try {
            Color.parseColor(item.color_hex) } catch (e: Exception) {
            Color.WHITE }
        val colorStateList = ColorStateList.valueOf(colorInt)

        if (holder is SimpleViewHolder) {
            holder.nombre.text = item.nombre
            holder.dias.text = item.dias_semana
            holder.icono.setImageResource(item.icono_res_id)

            holder.icono.imageTintList = colorStateList
            holder.iconCamara.imageTintList = colorStateList
            holder.iconMapa.imageTintList = colorStateList
            holder.check.backgroundTintList = colorStateList

            var updating = true
            holder.check.setOnCheckedChangeListener(null)
            holder.check.isChecked = item.completadoHoy
            updating = false

            holder.check.setOnCheckedChangeListener { _, isChecked ->
                if (updating) return@setOnCheckedChangeListener
                item.completadoHoy = isChecked
                onHabitChanged(item, isChecked)
            }

            holder.itemView.setOnClickListener {
                item.esComplejo = true // Cambiamos estado
                notifyItemChanged(position)
            }

        } else if (holder is ComplejoViewHolder) {
            holder.nombre.text = item.nombre
            holder.dias.text = item.dias_semana
            holder.icono.setImageResource(item.icono_res_id)

            holder.icono.imageTintList = colorStateList
            holder.btnFotoIcon.imageTintList = colorStateList
            holder.btnUbicacionIcon.imageTintList = colorStateList
            holder.btnCompletar.backgroundTintList = colorStateList

            // ── Botón foto ────────────────────────────────────────────────────
            holder.containerFoto.setOnClickListener {
                onTomarFoto(item)              // avisa al Fragment qué hábito está activo
            }

            holder.btnCompletar.setOnClickListener {
                item.completadoHoy = true
                item.esComplejo = false
                onHabitChanged(item, true)
                notifyItemChanged(position)
                Toast.makeText(holder.itemView.context, "✅ Hábito completado", Toast.LENGTH_SHORT).show()

            }

            holder.btnCompletarSin.setOnClickListener {
                item.esComplejo = false
                notifyItemChanged(position)
            }

            // Mostrar foto evidencia si ya existe
            if (item.fotoUrl != null) {
                holder.imgEvidencia.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(item.fotoUrl)
                    .centerCrop()
                    .into(holder.imgEvidencia)
            } else {
                holder.imgEvidencia.visibility = View.GONE
            }


        }

        holder.itemView.setOnLongClickListener {
            onHabitDeleted(item)
            true
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

        val containerFoto: View  = view.findViewById(R.id.containerFoto)
        val btnFotoIcon: ImageView = view.findViewById(R.id.btnFotoIcon)
        val btnUbicacionIcon: ImageView = view.findViewById(R.id.btnUbicacionIcon)
        val btnCompletar: Button = view.findViewById(R.id.btnCompletar)
        val btnCompletarSin: Button = view.findViewById(R.id.btnCompletarSin)

        val imgEvidencia: ImageView = view.findViewById(R.id.imgEvidencia)
    }
}