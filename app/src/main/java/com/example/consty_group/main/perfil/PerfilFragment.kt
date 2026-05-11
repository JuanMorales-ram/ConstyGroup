package com.example.consty_group.main.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.consty_group.R
import com.example.consty_group.admin.AdminActivity
import com.example.consty_group.data.HabitoRepository
import com.example.consty_group.data.UsuarioRepository
import com.example.consty_group.databinding.FragmentPerfilBinding
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarBotones()
        cargarDashboard()
    }

    private fun configurarBotones() {

        // Notificaciones
        binding.notiExpand.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContenedor, ConfigNotificacionesUsuarioFragment())
                .addToBackStack(null)
                .commit()
        }

        // Cuenta
        binding.cuentaExpand.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContenedor, CuentaFragment())
                .addToBackStack(null)
                .commit()
        }

        // Panel de Administrador (Ahora abre una nueva actividad)
        binding.adminExpand.setOnClickListener {
            val intent = Intent(requireContext(), AdminActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun cargarDashboard() {
        // Usamos viewLifecycleOwner para mayor seguridad en Fragmentos
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = UsuarioRepository.obtenerUsuario()
                val habitos = HabitoRepository.obtenerHabitos()
                val historial = HabitoRepository.obtenerHistorialCompleto()

                // Verificamos que el binding aún exista antes de tocar la UI
                _binding?.let { b ->
                    // 1. Datos personales
                    b.tvUsername.text = "${user?.nombre ?: ""} ${user?.apellidos ?: ""}"
                    b.tvEmail.text = user?.correo ?: ""

                    b.ivFotoPerfil.load(user?.foto_url) {
                        placeholder(R.drawable.usuariopic)
                        transformations(CircleCropTransformation())
                    }

                    // 2. Estadísticas rápidas
                    b.tvHabitos.text = habitos.size.toString()

                    // Días Activos: Es el conteo de fechas únicas
                    b.tvDiasActivos.text = historial.size.toString()

                    // Calcular racha más alta
                    var rachaMaxima = 0
                    habitos.forEach { habito ->
                        // Evitamos el !! usando ?. y el ID seguro
                        habito.id?.let { id ->
                            val registrosHabito = HabitoRepository.obtenerHistorialDeHabito(id)
                            val racha = HabitoRepository.calcularRacha(registrosHabito)
                            if (racha > rachaMaxima) rachaMaxima = racha
                        }
                    }
                    b.tvRachaActual.text = rachaMaxima.toString()
                }

            } catch (error: Exception) { // Cambiamos 'e' por 'error' para evitar conflictos con Log.e
                if (_binding != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
                error.printStackTrace()
            }
        }
    }

}