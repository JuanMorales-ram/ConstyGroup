package com.example.consty_group.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.consty_group.R

class InicioApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_app)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }


        val btnRegistro = findViewById<Button>(R.id.BtnRegistroInicioPage)
        btnRegistro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        val btnBtnIniciarSecionInicioPage = findViewById<Button>(R.id.BtnIniciarSecionInicioPage)
        btnBtnIniciarSecionInicioPage.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }


    }
}