package com.example.consty_group.main.abito

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.consty_group.data.Habito
import com.example.consty_group.data.HabitoRepository
import kotlinx.coroutines.launch


import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.consty_group.R
import com.example.consty_group.databinding.FragmentNuevoAbitoBinding
import java.util.Calendar

class NuevoAbitoFragment : Fragment() {

    private var _binding: FragmentNuevoAbitoBinding? = null
    private val binding get() = _binding!!

    // Lista de íconos disponibles
    private val iconosDisponibles = listOf(
        R.drawable.icono1, R.drawable.icono2, R.drawable.icono3, R.drawable.icono4,
        R.drawable.libro, R.drawable.icono6, R.drawable.cubiertos, R.drawable.icono7,
        R.drawable.icono8, R.drawable.icono9, R.drawable.icono15, R.drawable.icono10,
        R.drawable.icono11, R.drawable.icono12, R.drawable.icono13, R.drawable.camara
    )

    // Estado actual - Sincronizado con el primer icono de la lista
    private var iconoSeleccionado: Int = iconosDisponibles[0]
    private var colorSeleccionado: Int = Color.parseColor("#453AF9")
    private var horaSeleccionada: String = "8:00 PM"
    private val diasSeleccionados = mutableSetOf<String>()

    // Lista de colores disponibles
    private val coloresDisponibles = listOf(
        "#453AF9", "#FF4D50", "#FFA726", "#2ECC71",
        "#41CCFF", "#E91E63", "#9C27B0", "#FF5722",
        "#00BCD4", "#8BC34A", "#795548", "#607D8B",
        "#F06292", "#AED581", "#FFD54F", "#4DB6AC",
        "#CE93D8", "#FFAB91", "#80DEEA", "#A5D6A7"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevoAbitoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNombreHabito()
        configurarGridIconos()
        configurarGridColores()
        configurarSwitch()
        configurarDiasSemana()
        configurarHora()
        configurarBotones()
        actualizarVistaPrevia()
    }

    private fun configurarNombreHabito() {
        binding.etNombreHabito.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val count = s?.length ?: 0
                binding.tvContador.text = "$count/30"
                if (count > 30) {
                    binding.etNombreHabito.setText(s?.substring(0, 30))
                    binding.etNombreHabito.setSelection(30)
                }
                actualizarVistaPrevia()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun configurarGridIconos() {
        val iconoAdapter = IconoAdapter(iconosDisponibles) { icono ->
            iconoSeleccionado = icono
            actualizarVistaPrevia()
        }
        binding.rvIconos.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvIconos.adapter = iconoAdapter
    }

    private fun configurarGridColores() {
        val colorAdapter = ColorAdapter(coloresDisponibles) { color ->
            colorSeleccionado = Color.parseColor(color)
            actualizarVistaPrevia()
        }
        binding.rvColores.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvColores.adapter = colorAdapter
    }

    private fun configurarSwitch() {
        binding.switchRecordatorio.setOnCheckedChangeListener { _, isChecked ->
            binding.containerRecordatorio.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun configurarDiasSemana() {
        val dias = listOf(
            binding.diaLunes to "L",
            binding.diaMartes to "M",
            binding.diaMiercoles to "X",
            binding.diaJueves to "J",
            binding.diaViernes to "V",
            binding.diaSabado to "S",
            binding.diaDomingo to "D"
        )
        diasSeleccionados.addAll(listOf("L", "J"))
        dias.forEach { (view, letra) ->
            actualizarEstiloDia(view, diasSeleccionados.contains(letra))
            view.setOnClickListener {
                if (diasSeleccionados.contains(letra)) diasSeleccionados.remove(letra)
                else diasSeleccionados.add(letra)
                actualizarEstiloDia(view, diasSeleccionados.contains(letra))
                actualizarVistaPrevia()
            }
        }
    }

    private fun actualizarEstiloDia(view: TextView, seleccionado: Boolean) {
        val drawable = if (seleccionado) R.drawable.bg_dia_seleccionado else R.drawable.bg_dia_no_seleccionado
        view.background = ContextCompat.getDrawable(requireContext(), drawable)
    }

    private fun configurarHora() {
        binding.btnSeleccionarHora.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val hora12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                horaSeleccionada = String.format("%d:%02d %s", hora12, minute, amPm)
                binding.tvHora.text = horaSeleccionada
                actualizarVistaPrevia()
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }
    }

    private fun actualizarVistaPrevia() {
        val nombre = binding.etNombreHabito.text.toString().trim()

        binding.tvPreviewNombre.text = if (nombre.isNotEmpty()) nombre else "Tu habito aparecerá aquí"
        
        // Actualizar icono y su color
        binding.ivPreviewIcono.setImageResource(iconoSeleccionado)
        binding.ivPreviewIcono.imageTintList = ColorStateList.valueOf(colorSeleccionado)

        val diasTexto = if (diasSeleccionados.isEmpty()) "Sin días" else diasSeleccionados.sorted().joinToString("/")
        binding.tvPreviewHora.text = "$horaSeleccionada - $diasTexto"

        // SOLUCIÓN: Actualizamos el borde del contenedor sin afectar el fondo sólido
        // Esto evita que el icono se pierda al tener el mismo color que el fondo.
        val background = binding.containerVistaPrevia.background as? GradientDrawable
        background?.let {
            it.mutate()
            it.setStroke(dpToPx(4), colorSeleccionado)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun configurarBotones() {binding.btnVolver.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnCrearHabito.setOnClickListener {
            val nombre = binding.etNombreHabito.text.toString().trim()

            // 1. Validación simple
            if (nombre.isEmpty()) {
                binding.etNombreHabito.error = "Escribe un nombre para tu hábito"
                return@setOnClickListener
            }

            // 2. Recolectar el color en formato Hexadecimal para la base de datos
            // Convertimos el Int de colorSeleccionado a String Hex (ej: #453AF9)
            val colorHex = String.format("#%06X", 0xFFFFFF and colorSeleccionado)

            // 3. Crear el objeto Habito con lo que el usuario seleccionó en la UI
            val nuevoHabito = Habito(
                nombre = nombre,
                icono_res_id = iconoSeleccionado,
                color_hex = colorHex,
                recordatorio_activo = binding.switchRecordatorio.isChecked,
                hora_recordatorio = horaSeleccionada,
                dias_semana = diasSeleccionados.sorted().joinToString(",") // Guardamos "J,L"
            )

            // 4. Ejecutar la inserción en Supabase usando una Corrutina
            lifecycleScope.launch {
                try {
                    // Bloqueamos el botón para evitar doble clic
                    binding.btnCrearHabito.isEnabled = false

                    HabitoRepository.crearHabito(nuevoHabito)


                    // === AQUÍ SE ACTIVA LA NOTIFICACIÓN ===
                    if (nuevoHabito.recordatorio_activo) {
                        programarNotificacion(nuevoHabito.nombre, nuevoHabito.hora_recordatorio)
                    }

                    Toast.makeText(requireContext(), "¡Hábito \"$nombre\" creado!", Toast.LENGTH_SHORT).show()

                    // Volver a la pantalla anterior
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    binding.btnCrearHabito.isEnabled = true
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun programarNotificacion(nombreHabito: String, horaStr: String) {
        try {
            val intent = android.content.Intent(requireContext(), HabitoReceiver::class.java).apply {
                putExtra("nombre_habito", nombreHabito)
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                requireContext(),
                nombreHabito.hashCode(), // ID único para que no se borren entre sí
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = requireContext().getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager

            // Convertir la hora (ej: 8:00 PM) a milisegundos
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US)
            val date = sdf.parse(horaStr) ?: return
            val calendar = Calendar.getInstance().apply {
                val h = Calendar.getInstance()
                h.time = date
                set(Calendar.HOUR_OF_DAY, h.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, h.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)

                // Si la hora ya pasó hoy, programarla para mañana
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }

            // Programar alarma
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
