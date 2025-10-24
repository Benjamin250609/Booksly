package com.example.booksly.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Crea una única instancia del DataStore para toda la app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenciasRepository(private val context: Context) {

    // Claves para las preferencias
    private val PREF_KEY_IMAGEN_PERFIL_URI = stringPreferencesKey("imagen_perfil_uri")
    private val PREF_KEY_USUARIO_EMAIL = stringPreferencesKey("usuario_email_logueado")

    // Flow para la URI de la imagen de perfil
    val imagenPerfilUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_KEY_IMAGEN_PERFIL_URI]
        }

    // Flow para el email del usuario logueado
    val usuarioEmailFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_KEY_USUARIO_EMAIL]
        }

    // Guarda la URI de la imagen de perfil
    suspend fun guardarImagenPerfilUri(uriString: String?) {
        context.dataStore.edit { settings ->
            if (uriString == null) {
                settings.remove(PREF_KEY_IMAGEN_PERFIL_URI)
            } else {
                settings[PREF_KEY_IMAGEN_PERFIL_URI] = uriString
            }
        }
    }

    // Guarda el email del usuario logueado
    suspend fun guardarUsuarioEmail(email: String?) {
        context.dataStore.edit { settings ->
            if (email == null) {
                settings.remove(PREF_KEY_USUARIO_EMAIL)
            } else {
                settings[PREF_KEY_USUARIO_EMAIL] = email
            }
        }
    }
}
