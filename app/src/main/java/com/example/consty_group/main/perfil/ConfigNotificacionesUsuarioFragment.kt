package com.example.consty_group.main.perfil

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.consty_group.databinding.FragmentConfigNotificacionesUsuarioBinding
import java.util.Calendar

class ConfigNotificacionesUsuarioFragment : Fragment() {

    private var _binding: FragmentConfigNotificacionesUsuarioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigNotificacionesUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarSwitchPrincipal()
        configurarHoras()
        configurarVolver()
    }

    // Switch principal — activa/desactiva todos los demás
    private fun configurarSwitchPrincipal() {
        binding.switchTodas.setOnCheckedChangeListener { _, isChecked ->
            binding.switchMatutino.isEnabled = isChecked
            binding.switchVespertino.isEnabled = isChecked
            binding.switchNocturno.isEnabled = isChecked
            binding.switchLogros.isEnabled = isChecked
            binding.switchRachas.isEnabled = isChecked
            binding.switchMotivacion.isEnabled = isChecked
            binding.switchSonido.isEnabled = isChecked
            binding.switchVibracion.isEnabled = isChecked

            val alpha = if (isChecked) 1f else 0.4f
            binding.switchMatutino.alpha = alpha
            binding.switchVespertino.alpha = alpha
            binding.switchNocturno.alpha = alpha
            binding.switchLogros.alpha = alpha
            binding.switchRachas.alpha = alpha
            binding.switchMotivacion.alpha = alpha
            binding.switchSonido.alpha = alpha
            binding.switchVibracion.alpha = alpha
        }

        // Ocultar/mostrar hora según switch individual
        binding.switchMatutino.setOnCheckedChangeListener { _, isChecked ->
            binding.containerHoraMatutino.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.switchVespertino.setOnCheckedChangeListener { _, isChecked ->
            binding.containerHoraVespertino.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    // TimePicker para matutino y vespertino
    private fun configurarHoras() {
        binding.btnHoraMatutino.setOnClickListener {
            mostrarTimePicker { hora ->
                binding.tvHoraMatutino.text = hora
            }
        }

        binding.btnHoraVespertino.setOnClickListener {
            mostrarTimePicker { hora ->
                binding.tvHoraVespertino.text = hora
            }
        }
    }

    private fun mostrarTimePicker(onHoraSeleccionada: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, hour, minute ->
            val amPm = if (hour < 12) "AM" else "PM"
            val hora12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            onHoraSeleccionada(String.format("%d:%02d %s", hora12, minute, amPm))
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }

    private fun configurarVolver() {
        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}