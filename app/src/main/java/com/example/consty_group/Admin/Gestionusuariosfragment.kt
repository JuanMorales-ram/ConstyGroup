package com.example.consty_group.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consty_group.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

// ─────────────────────────────────────────────
// MODELO DE DATOS
// ─────────────────────────────────────────────

enum class EstadoUsuario { ACTIVO, INACTIVO }

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val avatar: String,          // emoji
    val racha: Int,
    val progreso: Int,           // 0-100
    val esPro: Boolean,
    val estado: EstadoUsuario,
    val ultimaConexion: String
)

// ─────────────────────────────────────────────
// FRAGMENT
// ─────────────────────────────────────────────

/**
 * GestionUsuariosFragment
 * ───────────────────────
 * Pantalla de gestión de usuarios del panel admin.
 * Archivo nuevo — NO modifica ningún archivo existente.
 *
 * Para navegar aquí:
 *   supportFragmentManager.beginTransaction()
 *       .replace(R.id.fragmentContainer, GestionUsuariosFragment())
 *       .addToBackStack(null)
 *       .commit()
 */
class GestionUsuariosFragment : Fragment() {

    private lateinit var adapter: UsuariosAdapter
    private var filtroActual = "todos"
    private var ordenActual  = "racha"

    // ── Datos de ejemplo (reemplazar con BD o API real) ──
    private val todosLosUsuarios = listOf(
        Usuario(1, "Andrés Torres",  "andres@email.com",  "🧑",  31, 90, true,  EstadoUsuario.ACTIVO,   "Hoy"),
        Usuario(2, "Sofía Martínez", "sofia@email.com",   "👩",  24, 75, true,  EstadoUsuario.ACTIVO,   "Hoy"),
        Usuario(3, "Isabella Díaz",  "isa@email.com",     "👩‍💻", 19, 60, true,  EstadoUsuario.ACTIVO,   "Hoy"),
        Usuario(4, "Carlos López",   "carlos@email.com",  "👨‍💼", 12, 45, false, EstadoUsuario.ACTIVO,   "Hoy"),
        Usuario(5, "Mariana Castro", "mari@email.com",    "👩‍🦱",  8, 30, false, EstadoUsuario.ACTIVO,   "Ayer"),
        Usuario(6, "Sebastián Gómez","seba@email.com",    "👦",   5, 20, false, EstadoUsuario.ACTIVO,   "Hoy"),
        Usuario(7, "Valentina Ruiz", "val@email.com",     "👩‍🦰",  0, 10, false, EstadoUsuario.INACTIVO, "Hace 7d"),
        Usuario(8, "Felipe Mora",    "felipe@email.com",  "👨‍🦳",  0,  8, false, EstadoUsuario.INACTIVO, "Hace 14d")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView(view)
        configurarFiltros(view)
        configurarBusqueda(view)
        configurarOrden(view)
        configurarFab(view)
        actualizarLista(view)
    }

    // ── RecyclerView ──
    private fun configurarRecyclerView(view: View) {
        adapter = UsuariosAdapter(emptyList()) { usuario ->
            // TODO: navegar a detalle del usuario
        }
        view.findViewById<RecyclerView>(R.id.rvUsuarios).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GestionUsuariosFragment.adapter
        }
    }

    // ── Filtros ──
    private fun configurarFiltros(view: View) {
        val botones = mapOf(
            "activos"   to view.findViewById<TextView>(R.id.btnFiltroActivos),
            "inactivos" to view.findViewById<TextView>(R.id.btnFiltroInactivos),
            "pro"       to view.findViewById<TextView>(R.id.btnFiltroPro),
            "todos"     to view.findViewById<TextView>(R.id.btnFiltroTodos)
        )
        botones.forEach { (clave, btn) ->
            btn.setOnClickListener {
                filtroActual = clave
                botones.values.forEach { b ->
                    b.setBackgroundResource(R.drawable.bg_filter_inactive)
                    b.setTextColor(Color.parseColor("#8888AA"))
                }
                btn.setBackgroundResource(R.drawable.bg_filter_active)
                btn.setTextColor(Color.WHITE)
                actualizarLista(view)
            }
        }
    }

    // ── Búsqueda ──
    private fun configurarBusqueda(view: View) {
        view.findViewById<EditText>(R.id.etBuscarUsuario)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    actualizarLista(view, query = s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })
    }

    // ── Ordenar ──
    private fun configurarOrden(view: View) {
        view.findViewById<TextView>(R.id.btnSortRacha).setOnClickListener {
            ordenActual = "racha"; actualizarLista(view)
        }
        view.findViewById<TextView>(R.id.btnSortProgreso).setOnClickListener {
            ordenActual = "progreso"; actualizarLista(view)
        }
        view.findViewById<TextView>(R.id.btnSortNombre).setOnClickListener {
            ordenActual = "nombre"; actualizarLista(view)
        }
    }

    // ── FAB ──
    private fun configurarFab(view: View) {
        view.findViewById<ExtendedFloatingActionButton>(R.id.fabNuevoUsuario).setOnClickListener {
            // TODO: abrir formulario para crear usuario
        }
    }

    // ── Filtrar + ordenar + actualizar ──
    private fun actualizarLista(view: View, query: String = "") {
        var lista = todosLosUsuarios

        // Filtro por estado / tipo
        lista = when (filtroActual) {
            "activos"   -> lista.filter { it.estado == EstadoUsuario.ACTIVO }
            "inactivos" -> lista.filter { it.estado == EstadoUsuario.INACTIVO }
            "pro"       -> lista.filter { it.esPro }
            else        -> lista
        }

        // Búsqueda
        if (query.isNotBlank()) {
            lista = lista.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true)
            }
        }

        // Orden
        lista = when (ordenActual) {
            "racha"    -> lista.sortedByDescending { it.racha }
            "progreso" -> lista.sortedByDescending { it.progreso }
            "nombre"   -> lista.sortedBy { it.nombre }
            else       -> lista
        }

        adapter.actualizar(lista)
        view.findViewById<TextView>(R.id.tvResultados).text = "${lista.size} resultados"
    }
}

// ─────────────────────────────────────────────
// ADAPTER
// ─────────────────────────────────────────────

class UsuariosAdapter(
    private var lista: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.VH>() {

    fun actualizar(nuevaLista: List<Usuario>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return VH(v)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(lista[position], onClick)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(u: Usuario, onClick: (Usuario) -> Unit) {
            itemView.setOnClickListener { onClick(u) }

            itemView.findViewById<TextView>(R.id.tvAvatar).text        = u.avatar
            itemView.findViewById<TextView>(R.id.tvNombreUsuario).text  = u.nombre
            itemView.findViewById<TextView>(R.id.tvEmailUsuario).text   = u.email
            itemView.findViewById<TextView>(R.id.tvRachaUsuario).text   = "${u.racha}"
            itemView.findViewById<TextView>(R.id.tvUltimaConexion).text = u.ultimaConexion

            // Badge PRO
            val tvPro = itemView.findViewById<TextView>(R.id.tvProBadge)
            tvPro.visibility = if (u.esPro) View.VISIBLE else View.GONE

            // Badge de avatar (solo pro)
            val tvBadge = itemView.findViewById<TextView>(R.id.tvAvatarBadge)
            tvBadge.visibility = if (u.esPro) View.VISIBLE else View.GONE

            // Progreso
            val colorBarra = if (u.estado == EstadoUsuario.ACTIVO) "#7C3AED" else "#F87171"
            val pb = itemView.findViewById<ProgressBar>(R.id.pbProgresoUsuario)
            pb.progress = u.progreso
            pb.progressTintList = ColorStateList.valueOf(Color.parseColor(colorBarra))

            // Racha color
            val rachaColor = if (u.racha > 0) "#FB923C" else "#F87171"
            itemView.findViewById<TextView>(R.id.tvRachaUsuario)
                .setTextColor(Color.parseColor(rachaColor))

            // Estado
            val tvEstado = itemView.findViewById<TextView>(R.id.tvEstadoUsuario)
            val viewDot  = itemView.findViewById<View>(R.id.viewStatusDot)
            if (u.estado == EstadoUsuario.ACTIVO) {
                tvEstado.text = "Activo"
                tvEstado.setTextColor(Color.parseColor("#22C55E"))
                viewDot.setBackgroundResource(R.drawable.bg_dot_green)
            } else {
                tvEstado.text = "Inactivo"
                tvEstado.setTextColor(Color.parseColor("#F87171"))
                viewDot.setBackgroundResource(R.drawable.bg_dot_red)
            }
        }
    }
}