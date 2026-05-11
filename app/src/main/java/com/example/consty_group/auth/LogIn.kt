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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.consty_group.Activities.ConfirmarCorreo
import com.example.consty_group.Activities.RecuperarContrasena
import com.example.consty_group.R
import com.example.consty_group.SupabaseClient
import com.example.consty_group.data.UsuarioRepository
import com.example.consty_group.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch

class LogIn : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputContrasena: EditText
    private lateinit var buttonLogIn: Button
    private lateinit var buttonGoogle: MaterialButton

    private lateinit var textRecupContra: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_log_in)

        val scrollView = findViewById<ViewGroup>(R.id.ScrollLogIn)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        inputEmail = findViewById(R.id.InputEmailLogIn)
        inputContrasena = findViewById(R.id.InputContrasenaLogIn)
        buttonLogIn = findViewById(R.id.ButtonLogIn)
        buttonGoogle = findViewById(R.id.ButtonGoogleLogIn)
        textRecupContra = findViewById(R.id.TextRecupContra)


        // ── Recuperar contraseña ─────────────────────────────────────────────
        textRecupContra.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasena::class.java))
        }


        // ── Login con email/contraseña ───────────────────────────────────────
        buttonLogIn.setOnClickListener {
            val emailDigitado = inputEmail.text.toString().trim()
            val contrasenaDigitada = inputContrasena.text.toString().trim()

            if (emailDigitado.isEmpty() || contrasenaDigitada.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        email    = emailDigitado
                        password = contrasenaDigitada
                    }
                    irAMainActivity()
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@LogIn,
                            "Error al iniciar sesión: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // ── Login con Google ─────────────────────────────────────────────────
        buttonGoogle.setOnClickListener { iniciarSesionConGoogle() }
    }

    private fun iniciarSesionConGoogle() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    // remplazamos con nuestro Web Client ID de Google Cloud Console

                    .setServerClientId("779250716737-mifc2n52ssi4bsqkujte21m6ljicmabo.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(this@LogIn)
                val result = credentialManager.getCredential(this@LogIn, request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                // Autenticar en Supabase con el token de Google
                SupabaseClient.client.auth.signInWith(IDToken) {
                    idToken = googleIdTokenCredential.idToken
                    provider = Google
                }

                // Si el usuario es nuevo, crear su fila en la tabla 'usuarios'
                val user = SupabaseClient.client.auth.currentUserOrNull()
                if (user != null) {
                    val existe = UsuarioRepository.existeUsuario(user.id)
                    if (!existe) {
                        val nombreCompleto = user.userMetadata
                            ?.get("full_name")?.toString()?.replace("\"", "") ?: " "
                        val nombre = nombreCompleto.split(" ").getOrNull(0) ?: ""
                        val apellidos = nombreCompleto.split(" ").drop(1).joinToString(" ")
                        UsuarioRepository.insertarUsuario(
                            userId = user.id,
                            nombre = nombre,
                            apellidos = apellidos,
                            correo = user.email ?: ""
                        )
                    }
                }

                irAMainActivity()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@LogIn,
                        "Error con Google: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun irAMainActivity() {
        runOnUiThread {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}