package com.example.consty_group

import android.net.http.HttpResponseCache.install
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        // ⚠️ REEMPLAZA estos valores con los de tu proyecto en Supabase
        // Los encuentras en: Supabase Dashboard → Settings → API
        supabaseUrl = "https://csjuxprivdrxogcvmncz.supabase.co",
        supabaseKey = "sb_publishable_83cq9RPp5JmgfLGaG4Zjug_okRiWNL9\n"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}