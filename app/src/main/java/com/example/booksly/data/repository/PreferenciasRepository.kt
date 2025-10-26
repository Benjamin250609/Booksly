package com.example.booksly.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión de Context para crear una única instancia de DataStore para toda la aplicación.
// Esto asegura que se use el mismo archivo de preferencias ("settings") en toda la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repositorio para gestionar las preferencias del usuario utilizando Jetpack DataStore.
 * Se encarga de guardar y leer datos simples de clave-valor, como el email del usuario
 * logueado o la URI de su imagen de perfil.
 */
class PreferenciasRepository(private val context: Context) {

    // Define las claves que se usarán para acceder a los valores en DataStore.
    private val PREF_KEY_IMAGEN_PERFIL_URI = stringPreferencesKey("imagen_perfil_uri")
    private val PREF_KEY_USUARIO_EMAIL = stringPreferencesKey("usuario_email_logueado")

    /**
     * Un [Flow] que emite la URI de la imagen de perfil del usuario cada vez que cambia.
     * Devuelve null si no hay ninguna URI guardada.
     */
    val imagenPerfilUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_KEY_IMAGEN_PERFIL_URI]
        }

    /**
     * Un [Flow] que emite el email del usuario logueado cada vez que cambia.
     * Se usa para determinar si una sesión está iniciada.
     */
    val usuarioEmailFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_KEY_USUARIO_EMAIL]
        }

    /**
     * Guarda la URI de la imagen de perfil en DataStore.
     * Si se pasa un valor nulo, se elimina la preferencia.
     */
    suspend fun guardarImagenPerfilUri(uriString: String?) {
        context.dataStore.edit { settings ->
            if (uriString == null) {
                settings.remove(PREF_KEY_IMAGEN_PERFIL_URI)
            } else {
                settings[PREF_KEY_IMAGEN_PERFIL_URI] = uriString
            }
        }
    }

    /**
     * Guarda el email del usuario que ha iniciado sesión.
     * Si se pasa un valor nulo, se elimina la preferencia (cierre de sesión).
     */
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
