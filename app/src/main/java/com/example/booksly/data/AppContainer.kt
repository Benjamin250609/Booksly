package com.example.booksly.data

import android.content.Context
import com.example.booksly.data.remote.AuthApiService
import com.example.booksly.data.remote.GoogleBooksApiService
import com.example.booksly.data.remote.RetrofitClient
import com.example.booksly.data.repository.AuthRepository
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.NotaRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.AppDatabase
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

interface AppContainer {
    val usuarioRepository: UsuarioRepository
    val libroRepository: LibroRepository
    val notaRepository: NotaRepository
    val preferenciasRepository: PreferenciasRepository
    val authRepository: AuthRepository
    val booksApiService: GoogleBooksApiService
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val BASE_URL = "http://100.30.140.229:8081/"

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonSerializer<LocalDate> { src, _, _ -> JsonPrimitive(src.toString()) }
        )
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonDeserializer<LocalDate> { json, _, _ -> LocalDate.parse(json.asString) }
        )
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    override val booksApiService: GoogleBooksApiService by lazy {
        RetrofitClient.instance
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(authApiService)
    }

    private val appDatabase : AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(appDatabase.usuarioDao())
    }

    override val libroRepository: LibroRepository by lazy {
        LibroRepository(appDatabase.libroDao())
    }

    override val notaRepository: NotaRepository by lazy {
        NotaRepository(appDatabase.notaDao())
    }

    override val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(context)
    }
}
