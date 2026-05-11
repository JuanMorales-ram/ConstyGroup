package com.example.consty_group.main.abito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.consty_group.R
import com.example.consty_group.data.HabitoRepository
import com.example.consty_group.databinding.FragmentRachaDeHabitosBinding
import kotlinx.coroutines.launch

class RachaDeHabitosFragment : Fragment() {

    private var _binding: FragmentRachaDeHabitosBinding? = null
    private val binding get() = _binding!!

    // Usamos el adaptador real
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

        // Configuramos la estructura del Recycler (2 columnas)
        binding.rvRachaHabitos.layoutManager = GridLayoutManager(requireContext(), 2)

        // Llamamos a la carga de datos reales
        cargarDatosReales()

        configurarBotones()
    }

    private fun cargarDatosReales() {
        lifecycleScope.launch {
            try {
                // 1. Obtener hábitos y registros de hoy de Supabase
                val habitos = HabitoRepository.obtenerHabitos()
                val completadosHoy = HabitoRepository.obtenerRegistrosHoy()

                val listaFinal = mutableListOf<Pair<com.example.consty_group.data.Habito, Int>>()

                // 2. Calcular la racha para cada hábito
                for (habito in habitos) {
                    // Marcamos si se hizo hoy para el icono visual del adaptador
                    habito.completadoHoy = completadosHoy.contains(habito.id)

                    // Obtenemos historial y calculamos racha
                    val historial = HabitoRepository.obtenerHistorialDeHabito(habito.id!!)
                    val numRacha = HabitoRepository.calcularRacha(historial)

                    listaFinal.add(habito to numRacha)
                }

                // 3. Inicializamos el adaptador con la lista real
                adapter = RachaHabitoAdapter(listaFinal)
                binding.rvRachaHabitos.adapter = adapter

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar rachas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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