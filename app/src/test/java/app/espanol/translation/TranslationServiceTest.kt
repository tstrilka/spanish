package app.espanol.translation

import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationServiceTest {

    @Test
    fun `translate returns correct Spanish word`() {
        val service = TranslationService()
        assertEquals("hola", service.translate("hello"))
    }
}
