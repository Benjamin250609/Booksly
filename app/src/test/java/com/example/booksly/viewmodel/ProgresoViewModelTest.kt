
package com.example.booksly.viewmodel

import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Libro
import com.example.booksly.model.Usuario
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
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
class ProgresoViewModelTest {

    private val libroRepository: LibroRepository = mockk(relaxed = true)
    private val usuarioRepository: UsuarioRepository = mockk(relaxed = true)
    private val preferenciasRepository: PreferenciasRepository = mockk(relaxed = true)
    private lateinit var viewModel: ProgresoViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val userEmailFlow = MutableStateFlow<String?>(null)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { preferenciasRepository.usuarioEmailFlow } returns userEmailFlow
    }

    private fun initializeViewModel() {
        viewModel = ProgresoViewModel(libroRepository, usuarioRepository, preferenciasRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Cuando no hay usuario, el estado de las estadísticas es el inicial")
    fun `sin usuario, estado inicial`() = runTest {
        userEmailFlow.value = null
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.estadisticasUiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.estadisticasUiState.value shouldBe EstadisticasUiState()

        job.cancel()
    }

    @Test
    @DisplayName("Cuando hay usuario, las estadísticas se cargan correctamente")
    fun `con usuario, las estadísticas se cargan`() = runTest {
        val email = "test@user.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val librosEnCurso = listOf(Libro(1, userId, "Libro 1", "Autor 1", "", 200, 100, "leyendo"))
        val librosFinalizados = listOf(Libro(2, userId, "Libro 2", "Autor 2", "", 300, 300, "finalizado"))
        
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.contarLibrosFinalizados(userId) } returns flowOf(1)
        coEvery { libroRepository.contarPaginasLeidas(userId) } returns flowOf(400)
        coEvery { libroRepository.obtenerLibrosPorEstado(userId, "leyendo") } returns flowOf(librosEnCurso)
        coEvery { libroRepository.obtenerLibrosPorEstado(userId, "finalizado") } returns flowOf(librosFinalizados)

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.estadisticasUiState.collect() }
        userEmailFlow.value = email
        testDispatcher.scheduler.advanceUntilIdle()

        with(viewModel.estadisticasUiState.value) {
            librosFinalizadosCount shouldBe 1
            totalPaginasLeidas shouldBe 400
            this.librosEnCurso shouldBe librosEnCurso
            this.librosFinalizadosList shouldBe librosFinalizados
        }

        job.cancel()
    }
}
