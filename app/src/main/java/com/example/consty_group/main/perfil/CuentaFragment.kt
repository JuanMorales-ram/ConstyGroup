package com.example.consty_group.main.perfil

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.consty_group.R
import com.example.consty_group.data.UsuarioRepository
import com.example.consty_group.databinding.FragmentCuentaBinding
import com.example.consty_group.main.MainActivity
import kotlinx.coroutines.launch

class CuentaFragment : Fragment() {

    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!

    // Selector de imágenes de la galería
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            subirImagenASupabase(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Cargar los datos actuales del usuario
        cargarDatos()

        // 2. Configurar clic para cambiar foto
        binding.ivFotoPerfilCuenta.setOnClickListener {
            pickImage.launch("image/*")
        }

        // 3. Configurar clic para guardar cambios de texto
        binding.btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        // 4. Botón volver
        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 5. Botón cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = UsuarioRepository.obtenerUsuario()
                user?.let {
                    binding.etNombreCuenta.setText(it.nombre)
                    binding.etCorreoCuenta.setText(it.correo)
                    // El correo suele ser de solo lectura en Auth
                    binding.etCorreoCuenta.isEnabled = false

                    if (!it.foto_url.isNullOrEmpty()) {
                        binding.ivFotoPerfilCuenta.load(it.foto_url) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            placeholder(R.drawable.usuariopic)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarCambios() {
        val nombre = binding.etNombreCuenta.text.toString().trim()
        val nuevaContrasena = binding.etNuevaContrasena.text.toString().trim()
        val confirmarContrasena = binding.etConfirmarContrasena.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.etNombreCuenta.error = "El nombre no puede estar vacío"
            return
        }

        if (nuevaContrasena.isNotEmpty()) {
            if (nuevaContrasena != confirmarContrasena) {
                binding.etConfirmarContrasena.error = "Las contraseñas no coinciden"
                return
            }
            if (nuevaContrasena.length < 6) {
                binding.etNuevaContrasena.error = "Mínimo 6 caracteres"
                return
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                UsuarioRepository.actualizarPerfil(nombre, "")
                if (nuevaContrasena.isNotEmpty()) {
                    UsuarioRepository.actualizarContrasena(nuevaContrasena)
                }
                Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun subirImagenASupabase(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Indicamos visualmente que se está subiendo (opcional)
                Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show()

                // 1. Subir al Storage y obtener URL
                val url = UsuarioRepository.subirFotoPerfil(bytes, "avatar.jpg")

                // 2. Actualizar la tabla de usuarios con la nueva URL
                val nombre = binding.etNombreCuenta.text.toString()
                UsuarioRepository.actualizarPerfil(nombre, "", url)

                // 3. Mostrar la nueva imagen
                binding.ivFotoPerfilCuenta.load(url) {
                    transformations(CircleCropTransformation())
                }

                //    para que el círculo pequeño arriba refleje la nueva foto.
                (activity as? MainActivity)?.cargarHeaderUsuario()

                Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cerrarSesion() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                UsuarioRepository.cerrarSesion()
                // Volver a la pantalla de login
                val intent = android.content.Intent(requireContext(),
                    com.example.consty_group.auth.LogIn::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}