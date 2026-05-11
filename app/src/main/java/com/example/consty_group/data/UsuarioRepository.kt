package com.example.consty_group.data

import com.example.consty_group.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.minutes

object UsuarioRepository {

    @Serializable
    data class UsuarioData(
        val id: String,
        val nombre: String,
        val apellidos: String,
        val correo: String? = null,
        val rol: String = "usuario",   // valores: "usuario", "admin"
        val foto_url: String? = null
    )

    /** Verifica si el usuario ya existe en la tabla pública 'usuarios' */
    suspend fun existeUsuario(userId: String): Boolean {
        return try {
            val resultado = SupabaseClient.client
                .postgrest["usuarios"]
                .select(Columns.raw("id")) {
                    filter { eq("id", userId) }
                }
                .decodeList<Map<String, String>>()
            resultado.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /** Inserta un usuario nuevo con rol por defecto "usuario" */
    suspend fun insertarUsuario(userId: String, nombre: String,apellidos: String, correo: String) {
        SupabaseClient.client.postgrest["usuarios"].upsert(
            UsuarioData(id = userId, nombre = nombre, apellidos = apellidos, correo = correo)
        ){
            onConflict = "id"

        }
    }

    /** Obtiene los datos del usuario actualmente autenticado */
    suspend fun obtenerUsuario(): UsuarioData? {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return null
        return try {
            SupabaseClient.client
                .postgrest["usuarios"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<UsuarioData>()
        } catch (e: Exception) {
            null
        }
    }

    /** Obtiene el rol del usuario actual. Retorna "usuario" si falla. */
    suspend fun obtenerRolActual(): String {
        return try {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return "usuario"
            val resultado = SupabaseClient.client
                .postgrest["usuarios"]
                .select(Columns.raw("rol")) {
                    filter { eq("id", userId) }
                }
                .decodeList<Map<String, String>>()
            resultado.firstOrNull()?.get("rol") ?: "usuario"
        } catch (e: Exception) {
            "usuario"
        }
    }

    /** Actualiza nombre, apellidos y foto del perfil */
    suspend fun actualizarPerfil(nombre: String, apellidos: String, fotoUrl: String? = null) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return

        val datos = buildJsonObject {
            put("nombre", nombre)
            put("apellidos", apellidos) // Ahora 'apellidos' vendrá del parámetro de la función
            if (fotoUrl != null) {
                put("foto_url", fotoUrl)
            }
        }

        try {
            SupabaseClient.client.postgrest["usuarios"]
                .update(datos) {
                    filter { eq("id", userId) }
                }
        } catch (e: Exception) {
            // manejamos el error o loguearlo
            e.printStackTrace()
        }
    }

    private val storage = SupabaseClient.client.storage["fotos_perfil"]

    /** Sube una imagen al storage y retorna su URL pública */
    suspend fun subirFotoPerfil(bytes: ByteArray, fileName: String): String {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: throw Exception("No auth")
        val path = "$userId/$fileName"

        // 1. Subir el archivo (sobrescribe si ya existe)
        storage.upload(path, bytes) {
            upsert = true
        }

        // 2. Obtener la URL pública
        return storage.publicUrl(path)
    }


    /** Actualiza la contraseña del usuario en Supabase Auth */
    suspend fun actualizarContrasena(nuevaContrasena: String) {
        SupabaseClient.client.auth.updateUser {
            password = nuevaContrasena
        }
    }

    /** Cierra la sesión del usuario */
    suspend fun cerrarSesion() {
        SupabaseClient.client.auth.signOut()
    }





}