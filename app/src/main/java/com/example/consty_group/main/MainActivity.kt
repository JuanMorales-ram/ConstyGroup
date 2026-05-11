package com.example.consty_group.main


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.consty_group.R
import com.example.consty_group.SupabaseClient
import com.example.consty_group.admin.AdminActivity
import com.example.consty_group.auth.LogIn
import com.example.consty_group.data.UsuarioRepository
import com.example.consty_group.main.abito.HistorialDeAvitosFragment
import com.example.consty_group.main.abito.HomeFragment
import com.example.consty_group.main.abito.NuevoAbitoFragment
import com.example.consty_group.main.abito.RachaDeHabitosFragment
import com.example.consty_group.main.perfil.PerfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import coil.load
import coil.transform.CircleCropTransformation
import com.example.consty_group.data.HabitoRepository

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Redirigir al login si no hay sesión activa
        if (SupabaseClient.client.auth.currentUserOrNull() == null) {
            startActivity(Intent(this, LogIn::class.java))
            finishAffinity()
            return
        }

        drawerLayout = findViewById(R.id.drawerLayoutMainPage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        //inicializar vistas y listeners
        val fotoPerfil = findViewById<ImageView>(R.id.ivFotoPerfilTop)
        fotoPerfil.setOnClickListener { cargarFragment(PerfilFragment()) }

        //cargar datos iniciales
        cargarFragment(HomeFragment())
        cargarHeaderUsuario()
        verificarRol()





        // Redirigir al login si no hay sesión activa
        if (SupabaseClient.client.auth.currentUserOrNull() == null) {
            startActivity(Intent(this, LogIn::class.java))
            finishAffinity()
            return
        }



        val mainLogo = findViewById<ImageView>(R.id.ivLogo)
        mainLogo.setOnClickListener { cargarFragment(HomeFragment()) }




        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.AdminAitos    -> cargarFragment(HistorialDeAvitosFragment())
                R.id.NuevAbito     -> cargarFragment(NuevoAbitoFragment())
                R.id.RachaDeAVitos -> cargarFragment(RachaDeHabitosFragment())
            }
            true
        }

        // Pedir permiso de notificaciones para Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }


    }

     fun cargarHeaderUsuario() {
        lifecycleScope.launch {
            val user = UsuarioRepository.obtenerUsuario()
            user?.let {
                val ivFotoTop = findViewById<ImageView>(R.id.ivFotoPerfilTop)
                if (!it.foto_url.isNullOrEmpty()) {
                    ivFotoTop.load(it.foto_url) {
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    /**
     * Consulta el rol del usuario en Supabase.
     * Si es "admin" redirige al AdminActivity automáticamente.
     * Si es "usuario" se queda en la pantalla normal.
     */
    private fun verificarRol() {
        lifecycleScope.launch {
            val rol = UsuarioRepository.obtenerRolActual()
            runOnUiThread {
                if (rol == "admin") {
                    startActivity(Intent(this@MainActivity, AdminActivity::class.java))
                    finish()
                }
                // Si es "usuario" no se hace nada, permanece aquí
            }
        }
    }

    /** Llama esto desde PerfilFragment o desde donde tengas el botón de cerrar sesión */
    fun cerrarSesion() {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                runOnUiThread {
                    startActivity(Intent(this@MainActivity, LogIn::class.java))
                    finishAffinity()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContenedor, fragment)
            .commit()
    }

    override fun onResume() {    super.onResume()
        // Cada vez que el usuario vuelve a la app o cierra un fragmento,
        // refrescamos la foto del círculo superior por si la cambió.
        cargarHeaderUsuario()
    }
}
