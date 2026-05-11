package com.example.consty_group.auth

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.consty_group.Activities.InicioApp
import com.example.consty_group.R
import com.example.consty_group.SupabaseClient
import com.example.consty_group.data.UsuarioRepository
import com.example.consty_group.main.MainActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class Registro : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputContrasena: EditText
    private lateinit var inputRepContrasena: EditText
    private lateinit var buttonComenzar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_registro)

        val scrollView = findViewById<ViewGroup>(R.id.ScrollRegistro)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left, systemBars.top, systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )
            insets
        }

        inputEmail = findViewById(R.id.InputEmailRegistro)
        inputContrasena = findViewById(R.id.InputContrasenaRegistro)
        inputRepContrasena = findViewById(R.id.ImputRepConRegistro)
        buttonComenzar = findViewById(R.id.ButtonComenzarRegistro)

        val nombreEmpresa = findViewById<TextView>(R.id.NombreEmpresaRegistro)
        nombreEmpresa.setOnClickListener {
            startActivity(Intent(this, InicioApp::class.java))
        }

        buttonComenzar.setOnClickListener {
            val correo = inputEmail.text.toString().trim()
            val contrasena = inputContrasena.text.toString().trim()
            val repContrasena = inputRepContrasena.text.toString().trim()

            when {
                correo.isEmpty() || contrasena.isEmpty() || repContrasena.isEmpty() ->
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                        .show()

                contrasena != repContrasena ->
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()

                contrasena.length < 8 ->
                    Toast.makeText(
                        this,
                        "La contraseña debe tener al menos 8 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()

                else -> registrar(correo, contrasena)
            }
        }
    }

    private fun registrar(correo: String, contrasena: String) {
        lifecycleScope.launch {
            try {
                // 1. Registrar en Supabase Auth y obtener el usuario creado
                val user = SupabaseClient.client.auth.signUpWith(Email) {
                    email = correo
                    password = contrasena
                    data = buildJsonObject {
                        put("nombre", correo.substringBefore("@"))
                    }
                }

                // 2. Insertar en tabla pública 'usuarios' usando el ID del usuario recién creado
                val userId = user?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
                if (!userId.isNullOrEmpty()) {
                    UsuarioRepository.insertarUsuario(
                        userId = userId,
                        nombre = correo.substringBefore("@"),
                        apellidos = "",
                        correo = correo
                    )
                }

                runOnUiThread {
                    Toast.makeText(this@Registro, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                    
                    // Si desactivas "Confirm Email" en Supabase, podemos ir directo al Main
                    startActivity(Intent(this@Registro, MainActivity::class.java))
                    finishAffinity()
                }

            } catch (e: Exception) {
                val mensaje = when {
                    e.message?.contains("already registered", ignoreCase = true) == true ->
                        "Este correo ya está registrado. Intenta iniciar sesión."
                    else -> "Error: ${e.message}"
                }
                runOnUiThread {
                    Toast.makeText(this@Registro, mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
