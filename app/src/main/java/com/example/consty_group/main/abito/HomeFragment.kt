package com.example.consty_group.main.abito

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.RecyclerAvitos)

        // ✅ LISTA CORRECTA (mutable + tipo base)
        val data = mutableListOf<AbitoItem>(
            AbitoSImple(
                "Meditación",
                12,
                requireContext().getColor(R.color.ColorAvito1),
                R.drawable.icono2
            ),
            AbitoSImple(
                "Ejercicio",
                5,
                requireContext().getColor(R.color.ColorAvito1),
                R.drawable.icono10
            )
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = AbitoAdapter(data)
    }
}