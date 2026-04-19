package com.example.consty_group.main.abito

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.consty_group.R
import com.example.consty_group.databinding.FragmentRachaDeHabitosBinding

class RachaDeHabitosFragment : Fragment() {

    private var _binding: FragmentRachaDeHabitosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RachaHabitoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRachaDeHabitosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        configurarBotones()
    }

    // Dentro de RachaDeHabitosFragment.kt
    private fun configurarRecyclerView() {
        val habitos = listOf(
            RachaHabito("Meditacion", R.drawable.icono7, 12, ColorRacha.AZUL, true),
            RachaHabito("Correr", R.drawable.icono2, 10, ColorRacha.AMARILLO, true),
            RachaHabito("Estudiar", R.drawable.icono5, 5, ColorRacha.CELESTE, true),
            RachaHabito("Amor", R.drawable.icono8, 3, ColorRacha.ROJO, true)
        )

        adapter = RachaHabitoAdapter(habitos)
        binding.rvRachaHabitos.adapter = adapter
    }

    private fun configurarBotones() {
        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.fabAgregarHabito.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContenedor, NuevoAbitoFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}