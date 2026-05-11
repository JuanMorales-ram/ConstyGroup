package com.example.consty_group.main.abito


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.consty_group.data.HabitoRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Usamos un scope de IO para consultar la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habitos = HabitoRepository.obtenerHabitos()
                val fragment = NuevoAbitoFragment()

                habitos
                    .filter { it.recordatorio_activo && it.hora_recordatorio.isNotEmpty() }
                    .forEach { habito ->
                        // Reprogramar cada alarma usando el mismo helper del fragment
                        reprogramarAlarma(context, habito.nombre, habito.hora_recordatorio)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun reprogramarAlarma(context: Context, nombreHabito: String, horaStr: String) {
        try {
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US)
            val fechaParseada = sdf.parse(horaStr) ?: return
            val calHora = java.util.Calendar.getInstance().apply { time = fechaParseada }

            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, calHora.get(java.util.Calendar.HOUR_OF_DAY))
                set(java.util.Calendar.MINUTE, calHora.get(java.util.Calendar.MINUTE))
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(java.util.Calendar.DATE, 1)
                }
            }

            val alarmIntent = Intent(context, HabitoReceiver::class.java).apply {
                putExtra("nombre_habito", nombreHabito)
                putExtra("hora_recordatorio", horaStr)
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                nombreHabito.hashCode(),
                alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    return
                }
            }

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