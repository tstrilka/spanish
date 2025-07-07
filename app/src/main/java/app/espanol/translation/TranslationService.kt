package app.espanol.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor() {

    private val czechToSpanishTranslator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.CZECH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        Translation.getClient(options)
    }

    private val spanishToCzechTranslator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(TranslateLanguage.CZECH)
            .build()
        Translation.getClient(options)
    }

    private var isInitialized = false

    suspend fun initializeTranslators() {
        if (!isInitialized) {
            try {
                val conditions = DownloadConditions.Builder()
                    .requireWifi()
                    .build()

                czechToSpanishTranslator.downloadModelIfNeeded(conditions).await()
                spanishToCzechTranslator.downloadModelIfNeeded(conditions).await()
                isInitialized = true
                app.espanol.util.Logger.i("Translation models downloaded")
            } catch (e: Exception) {
                app.espanol.util.Logger.e("Failed to download translation models", e)
            }
        }
    }

    suspend fun translate(text: String, isSpanishToCzech: Boolean = false): String {
        if (text.isBlank()) return ""

        // Fallback to dictionary for common words
        val dictionaryResult = getDictionaryTranslation(text, isSpanishToCzech)
        if (dictionaryResult != text) {
            return dictionaryResult
        }

        return try {
            if (!isInitialized) {
                initializeTranslators()
            }

            val translator = if (isSpanishToCzech) {
                spanishToCzechTranslator
            } else {
                czechToSpanishTranslator
            }

            translator.translate(text).await()
        } catch (e: Exception) {
            app.espanol.util.Logger.e("Translation failed", e)
            text // Return original text if translation fails
        }
    }

    private fun getDictionaryTranslation(text: String, isSpanishToCzech: Boolean): String {
        val normalized = text.lowercase().trim()

        return if (isSpanishToCzech) {
            spanishToCzech[normalized] ?: text
        } else {
            translations[normalized] ?: text
        }
    }

    private val translations = mapOf(
        "ahoj" to "hola",
        "dobrý den" to "buenos días",
        "dobré ráno" to "buenos días",
        "dobré odpoledne" to "buenas tardes",
        "dobrý večer" to "buenas noches",
        "na shledanou" to "adiós",
        "čau" to "adiós",
        "děkuji" to "gracias",
        "díky" to "gracias",
        "prosím" to "por favor",
        "promiňte" to "disculpe",
        "omlouvám se" to "lo siento",
        "není zač" to "de nada",
        "voda" to "agua",
        "jídlo" to "comida",
        "dům" to "casa",
        "auto" to "coche",
        "pes" to "perro",
        "kočka" to "gato",
        "kniha" to "libro",
        "škola" to "escuela",
        "práce" to "trabajo",
        "rodina" to "familia",
        "přítel" to "amigo",
        "peníze" to "dinero",
        "čas" to "tiempo",
        "láska" to "amor",
        "jak se máš?" to "¿cómo estás?",
        "jak se jmenuješ?" to "¿cómo te llamas?",
        "odkud jsi?" to "¿de dónde eres?",
        "kolik to stojí?" to "¿cuánto cuesta?",
        "kde je?" to "¿dónde está?",
        "jedna" to "uno",
        "dva" to "dos",
        "tři" to "tres",
        "čtyři" to "cuatro",
        "pět" to "cinco"
    )

    private val spanishToCzech = mapOf(
        "hola" to "ahoj",
        "buenos días" to "dobrý den",
        "buenas tardes" to "dobré odpoledne",
        "buenas noches" to "dobrý večer",
        "adiós" to "na shledanou",
        "gracias" to "děkuji",
        "por favor" to "prosím",
        "disculpe" to "promiňte",
        "lo siento" to "omlouvám se",
        "de nada" to "není zač",
        "agua" to "voda",
        "comida" to "jídlo",
        "casa" to "dům",
        "coche" to "auto",
        "perro" to "pes",
        "gato" to "kočka",
        "libro" to "kniha",
        "escuela" to "škola",
        "trabajo" to "práce",
        "familia" to "rodina",
        "amigo" to "přítel",
        "dinero" to "peníze",
        "tiempo" to "čas",
        "amor" to "láska",
        "¿cómo estás?" to "jak se máš?",
        "¿cómo te llamas?" to "jak se jmenuješ?",
        "¿de dónde eres?" to "odkud jsi?",
        "¿cuánto cuesta?" to "kolik to stojí?",
        "¿dónde está?" to "kde je?",
        "uno" to "jedna",
        "dos" to "dva",
        "tres" to "tři",
        "cuatro" to "čtyři",
        "cinco" to "pět"
    )

    fun cleanup() {
        czechToSpanishTranslator.close()
        spanishToCzechTranslator.close()
    }
}