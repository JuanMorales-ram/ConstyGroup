package com.example.consty_group.main


import android.os.Bundle
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.consty_group.R
import com.example.consty_group.main.abito.HistorialDeAvitosFragment
import com.example.consty_group.main.abito.HomeFragment
import com.example.consty_group.main.abito.NuevoAbitoFragment
import com.example.consty_group.main.abito.RachaDeAvitosFragment
import com.example.consty_group.main.perfil.PerfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.text.replace

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayoutMainPage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)


        val MainLogo = findViewById<ImageView>(R.id.ivLogo)
        MainLogo.setOnClickListener {
            CargarFragment(HomeFragment())
        }

        val FotoPerfil = findViewById<ImageView>(R.id.ivFotoPerfilTop)
        FotoPerfil.setOnClickListener {
            CargarFragment(PerfilFragment())
        }




        CargarFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.AdminAitos -> CargarFragment(HistorialDeAvitosFragment())
                R.id.NuevAbito -> CargarFragment(NuevoAbitoFragment())
                R.id.RachaDeAVitos -> CargarFragment(RachaDeAvitosFragment())

            }
            true
        }


    }

    private fun CargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContenedor, fragment)
            .commit()
    }

}


