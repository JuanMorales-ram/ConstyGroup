package com.example.consty_group.main.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import com.example.consty_group.admin.AdminActivity
import com.example.consty_group.databinding.FragmentPerfilBinding

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
}