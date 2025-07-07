package app.espanol.translation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationServiceTest {

    @Test
    fun `translate returns correct Spanish word from dictionary`() = runTest {
        val service = TranslationService()
        assertEquals("hola", service.translate("ahoj"))
    }

    @Test
    fun `translate returns correct Spanish phrase`() = runTest {
        val service = TranslationService()
        assertEquals("buenos días", service.translate("dobrý den"))
    }

    @Test
    fun `translate Spanish to Czech works`() = runTest {
        val service = TranslationService()
        assertEquals("ahoj", service.translate("hola", isSpanishToCzech = true))
    }

    @Test
    fun `translate returns original text for unknown words`() = runTest {
        val service = TranslationService()
        assertEquals("neznámé", service.translate("neznámé"))
    }

    @Test
    fun `translate returns empty string for blank input`() = runTest {
        val service = TranslationService()
        assertEquals("", service.translate(""))
        assertEquals("", service.translate("   "))
    }
}