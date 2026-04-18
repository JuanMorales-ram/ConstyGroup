package com.example.consty_group.main.abito

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var data: MutableList<AbitoItem>
    private lateinit var progressHabitos: ProgressBar
    private lateinit var txtPorcentaje: TextView
    private lateinit var txtHabitosCompletados: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components for progress
        progressHabitos = view.findViewById(R.id.progressHabitos)
        txtPorcentaje = view.findViewById(R.id.txtPorcentaje)
        txtHabitosCompletados = view.findViewById(R.id.txtHabitosCompletados)

        val recycler = view.findViewById<RecyclerView>(R.id.RecyclerAvitos)

        // Mock data with different colors to test dynamic coloring
        data = mutableListOf(
            AbitoSImple(
                "Meditación",
                12,
                Color.parseColor("#453AF9"), // Azul
                R.drawable.icono2
            ),
            AbitoSImple(
                "Ejercicio",
                5,
                Color.parseColor("#FF4D50"), // Rojo
                R.drawable.icono10
            ),
            AbitoSImple(
                "Lectura",
                3,
                Color.parseColor("#2ECC71"), // Verde
                R.drawable.icono1
            ),
            AbitoSImple(
                "Beber Agua",
                20,
                Color.parseColor("#FFA726"), // Naranja
                R.drawable.icono3
            )
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = AbitoAdapter(data) {
            updateProgress()
        }

        updateProgress() // Initial update
    }

    private fun updateProgress() {
        val total = data.size
        val completed = data.count { it.completadoHoy }
        
        val percentage = if (total > 0) (completed * 100) / total else 0
        
        progressHabitos.progress = percentage
        txtPorcentaje.text = "$percentage%"
        txtHabitosCompletados.text = "$completed de $total habitos completados"
    }
}