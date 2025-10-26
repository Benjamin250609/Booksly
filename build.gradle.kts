// Archivo de configuración de compilación de nivel superior donde puedes agregar opciones de configuración comunes a todos los submódulos/proyectos.
plugins {
    // Define el plugin de aplicación de Android, pero no lo aplica aquí (se aplicará en el módulo 'app').
    alias(libs.plugins.android.application) apply false
    // Define el plugin de Kotlin para Android, disponible para todos los módulos.
    alias(libs.plugins.kotlin.android) apply false
    // Define el plugin de Jetpack Compose, disponible para todos los módulos.
    alias(libs.plugins.kotlin.compose) apply false
    // Define el plugin 'kapt' para el procesamiento de anotaciones de Kotlin, pero no lo aplica globalmente.
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
}
