
package com.example.booksly.viewmodel

import com.example.booksly.data.remote.dto.UsuarioResponse
import com.example.booksly.data.repository.AuthRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Usuario
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class RegistroViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val usuarioRepository: UsuarioRepository = mockk()
    private lateinit var viewModel: RegistroViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegistroViewModel(authRepository, usuarioRepository) { email -> email.contains("@") }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("onNombreChange debería actualizar el nombre en el estado")
    fun `onNombreChange debería actualizar el nombre`() = runTest {
        val nombre = "Jane Doe"
        viewModel.onNombreChange(nombre)
        viewModel.uiState.value.nombre shouldBe nombre
        viewModel.uiState.value.nombreError shouldBe null
    }

    @Test
    @DisplayName("onEmailChange debería actualizar el email en el estado")
    fun `onEmailChange debería actualizar el email`() = runTest {
        val email = "jane.doe@example.com"
        viewModel.onEmailChange(email)
        viewModel.uiState.value.email shouldBe email
        viewModel.uiState.value.emailError shouldBe null
    }

    @Test
    @DisplayName("onClaveChange debería actualizar la clave en el estado")
    fun `onClaveChange debería actualizar la clave`() = runTest {
        val clave = "newpassword"
        viewModel.onClaveChange(clave)
        viewModel.uiState.value.clave shouldBe clave
        viewModel.uiState.value.claveError shouldBe null
    }

    @Test
    @DisplayName("onFechaDeNacimientoChange debería actualizar la fecha en el estado")
    fun `onFechaDeNacimientoChange debería actualizar la fecha`() = runTest {
        val fecha = "1990-01-01"
        viewModel.onFechaDeNacimientoChange(fecha)
        viewModel.uiState.value.fechaDeNacimiento shouldBe fecha
        viewModel.uiState.value.fechaDeNacimientoError shouldBe null
    }

    @Nested
    @DisplayName("Tests para onRegistroClick")
    inner class OnRegistroClick {

        @Test
        @DisplayName("Cuando el nombre está vacío, el estado debería mostrar error en el nombre")
        fun `onRegistroClick con nombre vacío debería mostrar error`() = runTest {
            // Given
            viewModel.onNombreChange("")
            viewModel.onEmailChange("test@test.com")
            viewModel.onClaveChange("password")
            viewModel.onFechaDeNacimientoChange("2000-01-01")

            // When
            viewModel.onRegistroClick()

            // Then
            val state = viewModel.uiState.value
            state.nombreError shouldBe "El nombre no puede estar vacío"
        }

        @Test
        @DisplayName("Cuando el email es inválido, el estado debería mostrar error en el email")
        fun `onRegistroClick con email inválido debería mostrar error`() = runTest {
            // Given
            viewModel.onNombreChange("Test User")
            viewModel.onEmailChange("test")
            viewModel.onClaveChange("password")
            viewModel.onFechaDeNacimientoChange("2000-01-01")

            // When
            viewModel.onRegistroClick()

            // Then
            val state = viewModel.uiState.value
            state.emailError shouldBe "Correo inválido"
        }

        @Test
        @DisplayName("Cuando la clave es muy corta, el estado debería mostrar error en la clave")
        fun `onRegistroClick con clave corta debería mostrar error`() = runTest {
            // Given
            viewModel.onNombreChange("Test User")
            viewModel.onEmailChange("test@test.com")
            viewModel.onClaveChange("12345")
            viewModel.onFechaDeNacimientoChange("2000-01-01")

            // When
            viewModel.onRegistroClick()

            // Then
            val state = viewModel.uiState.value
            state.claveError shouldBe "La contraseña debe tener al menos 6 caracteres"
        }

        @Test
        @DisplayName("Cuando la fecha es inválida, el estado debería mostrar error en la fecha")
        fun `onRegistroClick con fecha inválida debería mostrar error`() = runTest {
            // Given
            viewModel.onNombreChange("Test User")
            viewModel.onEmailChange("test@test.com")
            viewModel.onClaveChange("password")
            viewModel.onFechaDeNacimientoChange("2000-13-01") // Mes inválido

            // When
            viewModel.onRegistroClick()

            // Then
            val state = viewModel.uiState.value
            state.fechaDeNacimientoError shouldBe "Formato de fecha inválido (YYYY-MM-DD)"
        }

        @Test
        @DisplayName("Cuando el registro es exitoso, el estado debería ser exitoso y se guarda el usuario")
        fun `onRegistroClick con éxito debería ser exitoso y guardar usuario`() = runTest {
            // Given
            val nombre = "Test User"
            val email = "test@test.com"
            val clave = "password"
            val fechaNacimiento = LocalDate.of(2000, 1, 1)
            val usuarioResponse = UsuarioResponse(1L, nombre, email, fechaNacimiento)
            val usuarioLocal = Usuario(1, nombre, email, "", fechaNacimiento)

            coEvery { authRepository.registerUser(nombre, email, clave, fechaNacimiento) } returns Result.success(usuarioResponse)
            coEvery { usuarioRepository.insertUsuario(usuarioLocal) } just runs

            viewModel.onNombreChange(nombre)
            viewModel.onEmailChange(email)
            viewModel.onClaveChange(clave)
            viewModel.onFechaDeNacimientoChange(fechaNacimiento.toString())

            // When
            viewModel.onRegistroClick()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            state.registroExitoso shouldBe true
            coVerify(exactly = 1) { usuarioRepository.insertUsuario(usuarioLocal) }
        }

        @Test
        @DisplayName("Cuando el registro falla, el estado debería mostrar un mensaje de error general")
        fun `onRegistroClick con fallo debería mostrar error general`() = runTest {
            // Given
            val nombre = "Test User"
            val email = "test@test.com"
            val clave = "password"
            val fechaNacimiento = LocalDate.of(2000, 1, 1)
            val errorMessage = "El email ya existe"

            coEvery { authRepository.registerUser(nombre, email, clave, fechaNacimiento) } returns Result.failure(Exception(errorMessage))

            viewModel.onNombreChange(nombre)
            viewModel.onEmailChange(email)
            viewModel.onClaveChange(clave)
            viewModel.onFechaDeNacimientoChange(fechaNacimiento.toString())

            // When
            viewModel.onRegistroClick()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            state.mensajeErrorGeneral shouldBe errorMessage
            coVerify(exactly = 0) { usuarioRepository.insertUsuario(any()) }
        }
    }
}
