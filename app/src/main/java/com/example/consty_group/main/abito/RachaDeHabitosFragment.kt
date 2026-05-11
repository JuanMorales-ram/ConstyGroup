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
import com.example.consty_group.data.Habito
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
            try {val habitos = HabitoRepository.obtenerHabitos()
                val completadosHoy = HabitoRepository.obtenerRegistrosHoy()
                val listaFinal = mutableListOf<Pair<Habito, Int>>()

                for (habito in habitos) {
                    habito.completadoHoy = completadosHoy.containsKey(habito.id)

                    val numRacha = try {
                        val historial = HabitoRepository.obtenerHistorialDeHabito(habito.id ?: continue)
                        val rachaCalculada = HabitoRepository.calcularRacha(historial)

                        // LOG DE DEPURACIÓN
                        android.util.Log.d("RACHA_TEST", "Hábito: ${habito.nombre} | Registros: ${historial.size} | Racha: $rachaCalculada")

                        rachaCalculada
                    } catch (e: Exception) {
                        android.util.Log.e("RACHA_TEST", "Error en racha de ${habito.nombre}: ${e.message}")
                        0
                    }

                    listaFinal.add(habito to numRacha)
                }

                adapter = RachaHabitoAdapter(listaFinal)
                binding.rvRachaHabitos.adapter = adapter

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

    override fun onResume() {
        super.onResume()
        // Forzamos la recarga de datos cada vez que el usuario vuelve a esta pantalla
        cargarDatosReales()
    }

}