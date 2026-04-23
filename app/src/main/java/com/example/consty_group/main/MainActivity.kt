package com.example.consty_group.main


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import com.example.consty_group.admin.AdminActivity
import com.example.consty_group.main.abito.HistorialDeAvitosFragment
import com.example.consty_group.main.abito.HomeFragment
import com.example.consty_group.main.abito.NuevoAbitoFragment
import com.example.consty_group.main.abito.RachaDeHabitosFragment
import com.example.consty_group.main.perfil.PerfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayoutMainPage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)


        val mainLogo = findViewById<ImageView>(R.id.ivLogo)
        mainLogo.setOnClickListener {
            cargarFragment(HomeFragment())
        }

        val fotoPerfil = findViewById<ImageView>(R.id.ivFotoPerfilTop)
        fotoPerfil.setOnClickListener {
            cargarFragment(PerfilFragment())
        }

        cargarFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.AdminAitos -> cargarFragment(HistorialDeAvitosFragment())
                R.id.NuevAbito -> cargarFragment(NuevoAbitoFragment())
                R.id.RachaDeAVitos -> cargarFragment(RachaDeHabitosFragment())

            }
            true
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContenedor, fragment)
            .commit()
    }
}
