package com.example.consty_group.main.abito

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.consty_group.R

class HabitoReceiver : BroadcastReceiver() {


    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val nombreHabito = intent.getStringExtra("nombre_habito") ?: "Tu hábito"
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
    }
}