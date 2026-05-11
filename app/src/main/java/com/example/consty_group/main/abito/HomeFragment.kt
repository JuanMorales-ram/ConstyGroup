package com.example.consty_group.main.abito

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R
import com.example.consty_group.data.Habito
import com.example.consty_group.data.HabitoRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Cambiamos el tipo de dato a la clase Habito de Supabase
    private var dataHabitos = mutableListOf<Habito>()

    private lateinit var progressHabitos: ProgressBar
    private lateinit var txtPorcentaje: TextView
    private lateinit var txtHabitosCompletados: TextView
    private lateinit var adapter: AbitoAdapter


    // ── Cámara ────────────────────────────────────────────────────────────────
    private var habitoActual: Habito? = null   // hábito al que se le quiere agregar foto
    private var fotoUri: Uri? = null           // URI del archivo temporal de la foto

    /** Lanza la cámara y espera el resultado */
    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = fotoUri ?: return@registerForActivityResult
            val habito = habitoActual ?: return@registerForActivityResult
            subirFotoASupabase(uri, habito)
        }
    }

    /** Pide permiso de cámara y, si se otorga, abre la cámara */
    private val permisoCamaraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(requireContext(), "Permiso de cámara necesario", Toast.LENGTH_SHORT).show()
    }

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
                            HabitoRepository.toggleHabitoHoy(id, estaMarcado, habito.fotoUrl)
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
            },
            onTomarFoto = { habito ->
                habitoActual = habito
                solicitarCamaraOAbrir()
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // 3. Cargar datos reales desde Supabase
        cargarHabitos()
    }


    // ── Cámara ────────────────────────────────────────────────────────────────

    private fun solicitarCamaraOAbrir() {
        val permiso = android.Manifest.permission.CAMERA
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            requireContext(), permiso
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) abrirCamara() else permisoCamaraLauncher.launch(permiso)
    }

    private fun abrirCamara() {
        // Crear un archivo temporal donde la cámara guardará la foto
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val archivoFoto = File(requireContext().cacheDir, "fotos_habitos_$timestamp.jpg")
        fotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivoFoto
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        }
        camaraLauncher.launch(intent)
    }

    private fun subirFotoASupabase(uri: Uri, habito: Habito) {
        lifecycleScope.launch {
            try {
                // Leer los bytes de la foto desde la URI
                val bytes = requireContext().contentResolver
                    .openInputStream(uri)?.readBytes()
                    ?: run {
                        Toast.makeText(requireContext(), "No se pudo leer la foto", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName  = "foto_${habito.id}_$timestamp.jpg"

                // Subir a Supabase Storage → bucket "fotos_habitos"
                val urlPublica = HabitoRepository.subirFotoHabito(bytes, fileName, habito.id ?: "sin_id")

                habito.fotoUrl = urlPublica
                adapter.notifyDataSetChanged()

                //  guardar la URL en el registro de hoy
                habito.id?.let { id ->
                    android.util.Log.d("FOTO_DEBUG", "Llamando toggleHabitoHoy con URL: $urlPublica")

                    HabitoRepository.toggleHabitoHoy(id, true, urlPublica)
                    android.util.Log.d("FOTO_DEBUG", "toggleHabitoHoy completado")

                }

                Toast.makeText(requireContext(), "✅ Foto guardada", Toast.LENGTH_SHORT).show()
                android.util.Log.d("FOTO_HABITO", "URL: $urlPublica")

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al subir foto: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("FOTO_HABITO", "Error", e)
            }
        }
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
                    habito.fotoUrl = completadosHoy[habito.id] // ← recuperar la foto
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
