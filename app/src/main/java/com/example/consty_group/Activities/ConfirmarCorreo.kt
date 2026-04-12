package com.example.consty_group.Activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.consty_group.R

class ConfirmarCorreo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_confirmar_correo)
        val scrollView = findViewById<ViewGroup>(R.id.ScrollConfirmarCor)

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeIntents = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomPading = maxOf(systemBars.bottom, imeIntents.bottom)

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                bottomPading
            )
            insets

        }

        val BotonEnviarCodigo = findViewById<TextView>(R.id.ButtoVerificarCodigo)

        BotonEnviarCodigo.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasena::class.java))
        }
    }
}