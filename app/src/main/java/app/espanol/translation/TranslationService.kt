// Create a new file: app/src/main/java/app/espanol/translation/TranslationService.kt
package app.espanol.translation

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor() {

    private val translations = mapOf(
        // Greetings
        "hello" to "hola",
        "hi" to "hola",
        "goodbye" to "adiós",
        "good morning" to "buenos días",
        "good afternoon" to "buenas tardes",
        "good night" to "buenas noches",

        // Politeness
        "thank you" to "gracias",
        "thanks" to "gracias",
        "please" to "por favor",
        "excuse me" to "disculpe",
        "sorry" to "lo siento",
        "you're welcome" to "de nada",

        // Common words
        "water" to "agua",
        "food" to "comida",
        "house" to "casa",
        "car" to "coche",
        "dog" to "perro",
        "cat" to "gato",
        "book" to "libro",
        "school" to "escuela",
        "work" to "trabajo",
        "family" to "familia",
        "friend" to "amigo",
        "money" to "dinero",
        "time" to "tiempo",
        "love" to "amor",

        // Questions
        "how are you?" to "¿cómo estás?",
        "what is your name?" to "¿cómo te llamas?",
        "where are you from?" to "¿de dónde eres?",
        "how much?" to "¿cuánto cuesta?",
        "where is?" to "¿dónde está?",

        // Numbers
        "one" to "uno",
        "two" to "dos",
        "three" to "tres",
        "four" to "cuatro",
        "five" to "cinco"
    )

    fun translate(text: String): String {
        if (text.isBlank()) return ""

        val normalized = text.lowercase().trim()
        return translations[normalized] ?: text
    }

    fun addTranslation(english: String, spanish: String) {
        // For future expansion - could save to database
    }
}