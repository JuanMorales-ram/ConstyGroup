package com.example.consty_group


import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        // remplazamos estos valores con los de nuestro proyecto en Supabase
        supabaseUrl = "https://csjuxprivdrxogcvmncz.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNzanV4cHJpdmRyeG9nY3ZtbmN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgwNzQyMzQsImV4cCI6MjA5MzY1MDIzNH0.BTulpOVfUx5AKNUImqXrwgMWARP4YBjH2rZxdVAsakU"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}