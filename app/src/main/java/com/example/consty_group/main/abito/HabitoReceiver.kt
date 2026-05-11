package com.example.consty_group.main.abito

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.consty_group.R
import java.util.Calendar

class HabitoReceiver : BroadcastReceiver() {


    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val nombreHabito = intent.getStringExtra("nombre_habito") ?: "Tu hábito"
        val horaRecordatorio = intent.getStringExtra("hora_recordatorio") ?: ""
        val channelId = "HABITOS_NOTIFICACIONES"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación (necesario para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Hábitos",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.racha) // Asegúrate de que este icono existe
            .setContentTitle("¡Es hora de tu hábito!")
            .setContentText("No olvides completar: $nombreHabito")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación (usamos el hash del nombre como ID único)
        notificationManager.notify(nombreHabito.hashCode(), notification)

        if (horaRecordatorio.isNotEmpty()) {
            reprogramarAlarma(context, nombreHabito, horaRecordatorio)
    }
}

    private fun reprogramarAlarma(context: Context, nombreHabito: String, horaStr: String) {
        try {
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US)
            val fechaParseada = sdf.parse(horaStr) ?: return
            val calHora = Calendar.getInstance().apply { time = fechaParseada }

            // Programar para MAÑANA a la misma hora
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, calHora.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, calHora.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DATE, 1) // siempre mañana, ya que acaba de dispararse hoy
            }

            val nuevoIntent = Intent(context, HabitoReceiver::class.java).apply {
                putExtra("nombre_habito", nombreHabito)
                putExtra("hora_recordatorio", horaStr)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                nombreHabito.hashCode(),
                nuevoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
