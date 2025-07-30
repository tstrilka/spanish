package app.spanish.ui

import app.spanish.data.TextPairRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class TranslateViewModelTest {

    private lateinit var repository: TextPairRepository
    private lateinit var viewModel: TranslateViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = TranslateViewModel(repository)
    }

    @Test
    fun `initial state is idle`() = runTest {
        assertEquals(SaveState.Idle, viewModel.saveState.value)
    }

    @Test
    fun `reset save state works`() = runTest {
        viewModel.resetSaveState()
        assertEquals(SaveState.Idle, viewModel.saveState.value)
    }
}