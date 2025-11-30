
package com.example.booksly.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.NotaRepository
import com.example.booksly.model.Libro
import com.example.booksly.model.Nota
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class LibroDetalleViewModelTest {

    private val libroRepository: LibroRepository = mockk(relaxed = true)
    private val notaRepository: NotaRepository = mockk(relaxed = true)
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: LibroDetalleViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val libroId = 1
    private val libro = Libro(libroId, 1L, "Título", "Autor", "", 200, 50, "leyendo")

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle(mapOf("libroId" to libroId))
        coEvery { libroRepository.obtenerLibroPorId(libroId) } returns flowOf(libro)
        coEvery { notaRepository.obtenerNotasPorLibro(libroId) } returns flowOf(emptyList())
    }

    private fun initializeViewModel() {
        viewModel = LibroDetalleViewModel(libroRepository, notaRepository, savedStateHandle)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("Tests de Inicialización")
    inner class Inicializacion {
        @Test
        @DisplayName("Al iniciar, el estado se carga con el libro y las notas")
        fun `estado inicial se carga`() = runTest {
            val notas = listOf(Nota(1, libroId, "Nota 1"))
            coEvery { notaRepository.obtenerNotasPorLibro(libroId) } returns flowOf(notas)

            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            with(viewModel.uiState.value) {
                this.libro shouldBe libro
                this.notas shouldBe notas
                this.paginaActualInput shouldBe libro!!.paginaActual.toString()
                isLoading shouldBe false
            }
        }

        @Test
        @DisplayName("Si el libro no se encuentra, el estado no se actualiza")
        fun `libro no encontrado`() = runTest {
            coEvery { libroRepository.obtenerLibroPorId(libroId) } returns flowOf(null)
            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.libro shouldBe null
        }

        @Test
        @DisplayName("Si hay un error al cargar, el estado muestra el error")
        fun `error al cargar`() = runTest {
            val error = RuntimeException("Error de base de datos")
            coEvery { libroRepository.obtenerLibroPorId(libroId) } returns flow { throw error }

            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.errorCarga shouldBe "Error al cargar el libro y sus notas."
        }

        @Test
        @DisplayName("Lanza excepción si libroId no está en SavedStateHandle")
        fun `lanza excepcion si falta libroId`() {
            savedStateHandle = SavedStateHandle() // libroId is missing
            val exception = assertThrows<IllegalStateException> {
                initializeViewModel()
            }
            exception.message shouldBe "Required value was null."
        }
    }

    @Nested
    @DisplayName("Tests de Progreso de Lectura")
    inner class ProgresoLectura {

        @Test
        @DisplayName("onPaginaActualInputChange solo acepta dígitos")
        fun `onPaginaActualInputChange solo dígitos`() {
            initializeViewModel()
            viewModel.onPaginaActualInputChange("123")
            viewModel.uiState.value.paginaActualInput shouldBe "123"

            viewModel.onPaginaActualInputChange("abc")
            viewModel.uiState.value.paginaActualInput shouldBe "123" // No debe cambiar
        }

        @Test
        @DisplayName("guardarProgresoPagina con página inválida muestra error")
        fun `guardarProgresoPagina con página inválida`() {
            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onPaginaActualInputChange("300") // Página > total
            viewModel.guardarProgresoPagina()
            viewModel.uiState.value.errorPaginaInput shouldBe "Página inválida (0-200)"

            viewModel.onPaginaActualInputChange("-1") // Página < 0
            viewModel.guardarProgresoPagina()
            viewModel.uiState.value.errorPaginaInput shouldBe "Página inválida (0-200)"
        }

        @Test
        @DisplayName("guardarProgresoPagina actualiza el libro a 'leyendo'")
        fun `guardarProgresoPagina actualiza a leyendo`() = runTest {
            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onPaginaActualInputChange("100")
            viewModel.guardarProgresoPagina()
            testDispatcher.scheduler.advanceUntilIdle()

            val libroActualizado = libro.copy(paginaActual = 100, estado = "leyendo")
            coVerify { libroRepository.actualizarLibro(libroActualizado) }
            viewModel.uiState.value.progresoGuardado shouldBe true
        }

        @Test
        @DisplayName("guardarProgresoPagina actualiza el libro a 'finalizado'")
        fun `guardarProgresoPagina actualiza a finalizado`() = runTest {
            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onPaginaActualInputChange("200")
            viewModel.guardarProgresoPagina()
            testDispatcher.scheduler.advanceUntilIdle()

            val libroFinalizado = libro.copy(paginaActual = 200, estado = "finalizado")
            coVerify { libroRepository.actualizarLibro(libroFinalizado) }
            viewModel.uiState.value.progresoGuardado shouldBe true
        }
    }

    @Nested
    @DisplayName("Tests de Gestión de Notas")
    inner class GestionNotas {
        @Test
        @DisplayName("onNuevaNotaChange actualiza el texto de la nueva nota")
        fun `onNuevaNotaChange actualiza texto`() {
            initializeViewModel()
            viewModel.onNuevaNotaChange("Nuevo texto de nota")
            viewModel.uiState.value.nuevaNotaTexto shouldBe "Nuevo texto de nota"
        }

        @Test
        @DisplayName("guardarNuevaNota no inserta una nota vacía")
        fun `guardarNuevaNota con texto vacío`() = runTest {
            initializeViewModel()
            viewModel.onNuevaNotaChange("   ")
            viewModel.guardarNuevaNota()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { notaRepository.insertarNota(any()) }
        }

        @Test
        @DisplayName("guardarNuevaNota inserta una nota válida")
        fun `guardarNuevaNota inserta nota`() = runTest {
            initializeViewModel()
            val textoNota = "Esta es una nota válida."
            viewModel.onNuevaNotaChange(textoNota)
            viewModel.guardarNuevaNota()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { notaRepository.insertarNota(any()) }
            viewModel.uiState.value.nuevaNotaTexto shouldBe ""
        }

        @Test
        @DisplayName("eliminarNota llama al repositorio para eliminarla")
        fun `eliminarNota llama al repositorio`() = runTest {
            val nota = Nota(1, libroId, "Nota a eliminar")
            initializeViewModel()
            viewModel.eliminarNota(nota)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { notaRepository.eliminarNota(nota) }
        }
    }

    @Nested
    @DisplayName("Tests de Eliminación de Libro")
    inner class EliminacionLibro {

        @Test
        @DisplayName("onEliminarClick y onDismissEliminar gestionan el diálogo")
        fun `gestión del diálogo de eliminación`() {
            initializeViewModel()
            viewModel.onEliminarClick()
            viewModel.uiState.value.showConfirmacionEliminar shouldBe true

            viewModel.onDismissEliminar()
            viewModel.uiState.value.showConfirmacionEliminar shouldBe false
        }

        @Test
        @DisplayName("onConfirmarEliminar elimina el libro y actualiza el estado")
        fun `onConfirmarEliminar elimina libro`() = runTest {
            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onConfirmarEliminar()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { libroRepository.eliminarLibro(libro) }
            viewModel.uiState.value.libroEliminado shouldBe true
            viewModel.uiState.value.showConfirmacionEliminar shouldBe false
        }

        @Test
        @DisplayName("onConfirmarEliminar no hace nada si el libro es nulo")
        fun `onConfirmarEliminar con libro nulo`() = runTest {
            coEvery { libroRepository.obtenerLibroPorId(libroId) } returns flowOf(null)
            initializeViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onConfirmarEliminar()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { libroRepository.eliminarLibro(any()) }
        }
    }
}
