package app.espanol.data

import app.espanol.util.Logger
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TextPairRepositoryTest {

    private lateinit var dao: TextPairDao
    private lateinit var repository: TextPairRepository

    @Before
    fun setup() {
        mockkObject(Logger)
        every { Logger.i(any()) } just Runs
        every { Logger.e(any(), any()) } just Runs

        dao = mockk()
        repository = TextPairRepository(dao)
    }

    @Test
    fun `insertTextPair success`() = runTest {
        val pair = TextPair(original = "hello", translated = "hola")
        coEvery { dao.insert(pair) } returns 1L

        val result = repository.insertTextPair(pair)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `insertTextPair fails with empty text`() = runTest {
        val pair = TextPair(original = "", translated = "hola")

        val result = repository.insertTextPair(pair)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `getAllTextPairs returns flow`() = runTest {
        val pairs = listOf(TextPair(original = "hello", translated = "hola"))
        every { dao.getAll() } returns flowOf(pairs)

        val result = repository.getAllTextPairs().first()

        assertEquals(pairs, result)
    }
}