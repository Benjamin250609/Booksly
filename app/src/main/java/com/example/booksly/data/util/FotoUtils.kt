package com.example.booksly.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FotoUtils {

    fun crearUriTemporal(context: Context): Uri {
        // Define el directorio donde se guardarán las imágenes (privado para la app)
        val directorio = File(context.externalCacheDir, "fotos_temp")
        directorio.mkdirs() // Crea el directorio si no existe

        // Crea un nombre de archivo único basado en la fecha y hora
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val archivo = File.createTempFile(
            "JPEG_${timestamp}_", // Prefijo
            ".jpg",              // Sufijo
            directorio           // Directorio
        )

        // Obtiene la URI para el archivo usando FileProvider
        // La autoridad debe coincidir con la definida en AndroidManifest.xml
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }
}
