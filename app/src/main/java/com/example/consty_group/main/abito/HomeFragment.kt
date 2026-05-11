package com.example.consty_group.main.abito

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R
import com.example.consty_group.data.Habito
import com.example.consty_group.data.HabitoRepository
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Cambiamos el tipo de dato a la clase Habito de Supabase
    private var dataHabitos = mutableListOf<Habito>()

    private lateinit var progressHabitos: ProgressBar
    private lateinit var txtPorcentaje: TextView
    private lateinit var txtHabitosCompletados: TextView
    private lateinit var adapter: AbitoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar componentes de la UI
        progressHabitos = view.findViewById(R.id.progressHabitos)
        txtPorcentaje = view.findViewById(R.id.txtPorcentaje)
        txtHabitosCompletados = view.findViewById(R.id.txtHabitosCompletados)
        val recycler = view.findViewById<RecyclerView>(R.id.RecyclerAvitos)

        // 2. Configurar el Adaptador con la lógica de Supabase
        adapter = AbitoAdapter(
            dataHabitos,
            { habito, estaMarcado ->
                // PARÁMETRO 1: Lógica de cambio (la que ya tenías)
                lifecycleScope.launch {
                    try {
                        habito.id?.let { id ->
                            HabitoRepository.toggleHabitoHoy(id, estaMarcado)
                            updateProgress()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HABITO_BUG", "Error al guardar progreso", e)
                        Toast.makeText(requireContext(), "Error al guardar progreso", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            { habitoABorrar ->
                // PARÁMETRO 2: Lógica de borrado (NUEVA)
                mostrarDialogoEliminar(habitoABorrar)
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // 3. Cargar datos reales desde Supabase
        cargarHabitos()
    }

    private fun cargarHabitos() {
        lifecycleScope.launch {
            try {
                // 1. Descargar todos los hábitos del usuario
                val listaNube = HabitoRepository.obtenerHabitos()

                // 2. Descargar qué IDs se completaron hoy
                val completadosHoy = HabitoRepository.obtenerRegistrosHoy()

                // 3. Cruzar la información
                listaNube.forEach { habito ->
                    // Si el ID del hábito está en la lista de completados, marcarlo
                    habito.completadoHoy = completadosHoy.contains(habito.id)
                }

                // 4. Actualizar la lista local y el adaptador
                dataHabitos.clear()
                dataHabitos.addAll(listaNube)

                adapter.notifyDataSetChanged()
                updateProgress() // Esto pone la barra de progreso en su lugar correcto al iniciar

            } catch (e: Exception) {
                android.util.Log.e("HABITO_BUG", "Error al sincronizar", e)
                Toast.makeText(requireContext(), "Error al sincronizar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgress() {
        val total = dataHabitos.size
        val completed = dataHabitos.count { it.completadoHoy }

        val percentage = if (total > 0) (completed * 100) / total else 0

        progressHabitos.progress = percentage
        txtPorcentaje.text = "$percentage%"
        txtHabitosCompletados.text = "$completed de $total habitos completados"
    }


    //funcion paras mostrar mensaje al intentar eliminar un hábito
    private fun mostrarDialogoEliminar(habito: Habito) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar hábito?")
            .setMessage("¿Estás seguro de que quieres borrar \"${habito.nombre}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        habito.id?.let { id ->
                            HabitoRepository.eliminarHabito(id)
                            dataHabitos.remove(habito)
                            adapter.notifyDataSetChanged()
                            updateProgress()
                            Toast.makeText(requireContext(), "Hábito eliminado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }




}
