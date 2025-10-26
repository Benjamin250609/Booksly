plugins {
    // Plugins de Android, Kotlin y Compose para la aplicación.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Plugin para el procesamiento de anotaciones de Kotlin (usado por Room).
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.booksly"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.booksly"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Se establece la compatibilidad con Java 11.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // Habilita Jetpack Compose y la generación de BuildConfig.
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Versión del compilador de Kotlin para Compose.
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

// Configuración para el procesador de anotaciones de Kotlin.
kapt {
    correctErrorTypes = true
}

dependencies {

    // --- Dependencias de Jetpack Compose y UI ---
    implementation("androidx.compose.material:material-icons-extended-android:1.6.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // --- Persistencia de Datos (Room y DataStore) ---
    implementation(libs.room.runtime)
    kapt(libs.room.compiler) // Procesador de anotaciones para Room.
    implementation(libs.room.ktx) // Extensiones de Kotlin para corrutinas con Room.
    implementation(libs.datastore.prefs) // Para almacenamiento de datos clave-valor.

    // --- Red (Retrofit y OkHttp) ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson) // Convertidor de JSON.
    implementation(libs.okhttp.logging) // Interceptor para logs de red.

    // --- ViewModel --- 
    implementation(libs.lifecycle.viewmodel.compose)

    // --- Carga de Imágenes (Coil) ---
    implementation(libs.coil.compose)

    // --- Dependencias de Pruebas ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
