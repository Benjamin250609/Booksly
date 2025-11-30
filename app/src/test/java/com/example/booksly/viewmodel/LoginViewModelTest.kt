
package com.example.booksly.viewmodel

import com.example.booksly.data.remote.dto.UsuarioResponse
import com.example.booksly.data.repository.AuthRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Usuario
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class LoginViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val usuarioRepository: UsuarioRepository = mockk()
    private val preferenciasRepository: PreferenciasRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository, usuarioRepository, preferenciasRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("onEmailChange debería actualizar el email en el estado")
    fun `onEmailChange debería actualizar el email`() = runTest {
        val newEmail = "test@example.com"
        viewModel.onEmailChange(newEmail)
        viewModel.email.value shouldBe newEmail
    }

    @Test
    @DisplayName("onPasswordChange debería actualizar la contraseña en el estado")
    fun `onPasswordChange debería actualizar la contraseña`() = runTest {
        val newPassword = "password123"
        viewModel.onPasswordChange(newPassword)
        viewModel.password.value shouldBe newPassword
    }

    @Nested
    @DisplayName("Tests para onLoginClicked")
    inner class OnLoginClicked {

        @Test
        @DisplayName("Cuando el email está vacío, el estado debería ser Error")
        fun `onLoginClicked con email vacío debería emitir Error`() = runTest {
            // Given
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("password")

            // When
            viewModel.onLoginClicked()

            // Then
            val state = viewModel.uiState.first()
            state.shouldBeInstanceOf<LoginUiState.Error>()
            (state as LoginUiState.Error).message shouldBe "Email y contraseña no pueden estar vacíos"
        }

        @Test
        @DisplayName("Cuando la contraseña está vacía, el estado debería ser Error")
        fun `onLoginClicked con contraseña vacía debería emitir Error`() = runTest {
            // Given
            viewModel.onEmailChange("test@test.com")
            viewModel.onPasswordChange("")

            // When
            viewModel.onLoginClicked()

            // Then
            val state = viewModel.uiState.first()
            state.shouldBeInstanceOf<LoginUiState.Error>()
            (state as LoginUiState.Error).message shouldBe "Email y contraseña no pueden estar vacíos"
        }

        @Test
        @DisplayName("Cuando el login es exitoso, el estado debería ser Success y se guardan los datos")
        fun `onLoginClicked con éxito debería emitir Success y guardar datos`() = runTest {
            // Given
            val email = "test@test.com"
            val password = "password"
            val birthDate = LocalDate.of(2000, 1, 1)
            val userResponse = UsuarioResponse(
                id = 1L,
                username = "Test User",
                email = email,
                fechaNacimiento = birthDate
            )
            val expectedUser = Usuario(
                id = 1,
                nombre = "Test User",
                email = email,
                clave = "",
                fechaDeNacimiento = birthDate
            )
            
            coEvery { authRepository.loginUser(email, password) } returns Result.success(userResponse)
            coEvery { usuarioRepository.insertUsuario(any()) } just runs
            coEvery { preferenciasRepository.guardarUsuarioEmail(email) } just runs

            // When
            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onLoginClicked()

            // Then
            testDispatcher.scheduler.advanceUntilIdle() // Asegura que todas las corrutinas se completen
            
            val state = viewModel.uiState.value
            state.shouldBeInstanceOf<LoginUiState.Success>()
            (state as LoginUiState.Success).user shouldBe userResponse

            coVerify(exactly = 1) { usuarioRepository.insertUsuario(expectedUser) }
            coVerify(exactly = 1) { preferenciasRepository.guardarUsuarioEmail(email) }
        }

        @Test
        @DisplayName("Cuando el login falla, el estado debería ser Error")
        fun `onLoginClicked con fallo debería emitir Error`() = runTest {
            // Given
            val email = "test@test.com"
            val password = "wrongpassword"
            val errorMessage = "Credenciales inválidas"
            
            coEvery { authRepository.loginUser(email, password) } returns Result.failure(Exception(errorMessage))

            // When
            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onLoginClicked()

            // Then
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.shouldBeInstanceOf<LoginUiState.Error>()
            (state as LoginUiState.Error).message shouldBe errorMessage

            coVerify(exactly = 0) { usuarioRepository.insertUsuario(any()) }
            coVerify(exactly = 0) { preferenciasRepository.guardarUsuarioEmail(any()) }
        }
    }
    
    @Test
    @DisplayName("resetState debería devolver el estado a Idle")
    fun `resetState debería cambiar uiState a Idle`() = runTest {
        // Given
        viewModel.onLoginClicked() // Pone un estado de error
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.resetState()

        // Then
        val state = viewModel.uiState.value
        state shouldBe LoginUiState.Idle
    }
}
