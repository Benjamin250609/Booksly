package com.example.booksly.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Interfaz del servicio de Retrofit para la API de Google Books
interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(@Query("q") query: String): Response<BookSearchResponse>
}

// Modelos de datos para la respuesta de la API
data class BookSearchResponse(
    val items: List<VolumeItem>?
)

data class VolumeItem(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val pageCount: Int?,
    val imageLinks: ImageLinks?
)

data class ImageLinks(
    @SerializedName("thumbnail")
    val thumbnailUrl: String?
)

// Objeto para crear y proveer la instancia de Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    val instance: GoogleBooksApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GoogleBooksApiService::class.java)
    }
}
