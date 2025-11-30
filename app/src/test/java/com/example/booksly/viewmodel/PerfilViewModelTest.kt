
package com.example.booksly.viewmodel

import android.net.Uri
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Usuario
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class PerfilViewModelTest {

    private val preferenciasRepository: PreferenciasRepository = mockk(relaxed = true)
    private val usuarioRepository: UsuarioRepository = mockk()
    private val libroRepository: LibroRepository = mockk()
    private lateinit var viewModel: PerfilViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val userEmailFlow = MutableStateFlow<String?>(null)
    private val profileImageUriFlow = MutableStateFlow<String?>(null)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class)
        coEvery { Uri.parse(any()) } returns mockk()

        coEvery { preferenciasRepository.usuarioEmailFlow } returns userEmailFlow
        coEvery { preferenciasRepository.imagenPerfilUriFlow } returns profileImageUriFlow
    }

    private fun initializeViewModel() {
        viewModel = PerfilViewModel(preferenciasRepository, usuarioRepository, libroRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Cuando no hay usuario, el estado de la UI tiene valores por defecto")
    fun `sin usuario, uiState tiene valores por defecto`() = runTest {
        userEmailFlow.value = null
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        state.nombreUsuario shouldBe "Usuario"
        state.emailUsuario shouldBe ""
        state.imagenUri shouldBe null
        state.librosTerminados shouldBe 0
        state.paginasLeidas shouldBe 0
        job.cancel()
    }

    @Test
    @DisplayName("Cuando hay usuario y con foto, el estado de la UI se actualiza")
    fun `con usuario y foto, uiState se actualiza`() = runTest {
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val imagenUriString = "content://com.example.booksly/image/1"
        val mockedUri: Uri = mockk()
        val librosTerminados = 5
        val paginasLeidas = 1234
        
        coEvery { Uri.parse(imagenUriString) } returns mockedUri
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.contarLibrosFinalizados(userId) } returns flowOf(librosTerminados)
        coEvery { libroRepository.contarPaginasLeidas(userId) } returns flowOf(paginasLeidas)

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userEmailFlow.value = email
        profileImageUriFlow.value = imagenUriString

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        state.nombreUsuario shouldBe usuario.nombre
        state.emailUsuario shouldBe usuario.email
        state.imagenUri shouldBe mockedUri
        state.librosTerminados shouldBe librosTerminados
        state.paginasLeidas shouldBe paginasLeidas
        job.cancel()
    }
    
    @Test
    @DisplayName("Cuando hay usuario sin foto, el estado de la UI se actualiza y la imagen es nula")
    fun `con usuario sin foto, imagenUri es nulo`() = runTest {
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.contarLibrosFinalizados(any()) } returns flowOf(0)
        coEvery { libroRepository.contarPaginasLeidas(any()) } returns flowOf(0)

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userEmailFlow.value = email
        profileImageUriFlow.value = null // Sin foto

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        state.nombreUsuario shouldBe usuario.nombre
        state.emailUsuario shouldBe usuario.email
        state.imagenUri shouldBe null
        job.cancel()
    }

    @Test
    @DisplayName("onImagenSeleccionada con URI no nulo debe guardar el URI")
    fun `onImagenSeleccionada con URI no nulo`() = runTest {
        initializeViewModel()
        val uriString = "content://com.example.booksly/new_image"
        val mockedUri: Uri = mockk()
        coEvery { mockedUri.toString() } returns uriString
        coEvery { preferenciasRepository.guardarImagenPerfilUri(any()) } just runs

        viewModel.onImagenSeleccionada(mockedUri)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { preferenciasRepository.guardarImagenPerfilUri(uriString) }
    }
    
    @Test
    @DisplayName("onImagenSeleccionada con URI nulo debe guardar null")
    fun `onImagenSeleccionada con URI nulo`() = runTest {
        initializeViewModel()
        coEvery { preferenciasRepository.guardarImagenPerfilUri(null) } just runs

        viewModel.onImagenSeleccionada(null)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { preferenciasRepository.guardarImagenPerfilUri(null) }
    }

    @Test
    @DisplayName("cerrarSesion debe limpiar las preferencias y activar el estado de logout")
    fun `cerrarSesion limpia preferencias`() = runTest {
        initializeViewModel()
        coEvery { preferenciasRepository.guardarUsuarioEmail(null) } just runs
        coEvery { preferenciasRepository.guardarImagenPerfilUri(null) } just runs

        viewModel.cerrarSesion()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { preferenciasRepository.guardarUsuarioEmail(null) }
        coVerify(exactly = 1) { preferenciasRepository.guardarImagenPerfilUri(null) }
        viewModel.logoutExitoso.value shouldBe true
    }
}
