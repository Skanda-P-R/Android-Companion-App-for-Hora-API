package com.hora.companion.utils

object TranslationUtils {
    private val UI = mapOf(
        "Current Hora" to "ಪ್ರಸ್ತುತ ಹೋರೆ",
        "Remaining" to "ಉಳಿದ ಸಮಯ",
        "Updated" to "ನವೀಕರಿಸಲಾಗಿದೆ",
        "Tithi" to "ತಿಥಿ",
        "Nakshatra" to "ನಕ್ಷತ್ರ",
        "Yoga" to "ಯೋಗ",
        "Karana" to "ಕರಣ",
        "Vara" to "ವಾರ",
        "Sunrise" to "ಸೂರ್ಯೋದಯ",
        "Sunset" to "ಸೂರ್ಯಾಸ್ತ",
        "Moon Rasi" to "ಚಂದ್ರ ರಾಶಿ",
        "Sun Rasi" to "ಸೂರ್ಯ ರಾಶಿ",
        "Abhijit" to "ಅಭಿಜಿತ್",
        "Rahu Kalam" to "ರಾಹುಕಾಲ",
        "Yamaganda" to "ಯಮಗಂಡ",
        "Ends" to "ಅಂತ್ಯ",
        "Next" to "ಮುಂದಿನ ಹೋರೆ",
        "Next Hora" to "ಮುಂದಿನ ಹೋರೆ",
        "Full Panchanga" to "ಪೂರ್ಣ ಪಂಚಾಂಗ",
        "View Kundali" to "ಕುಂಡಲಿ ವೀಕ್ಷಿಸಿ",
        "Settings" to "ಸೇಟಿಂಗ್ಸ್",
        "Limbs" to "ಅಂಗಗಳು",
        "Solar & Celestial" to "ಸೌರ ಮತ್ತು ಚಂದ್ರ",
        "Timings" to "ಸಮಯಗಳು",
        "Hora" to "ಹೋರೆ",
        "Calendar" to "ಪಂಚಾಂಗ ವಿವರ",
        "Samvatsara" to "ಸಂವತ್ಸರ",
        "Ayana" to "ಅಯನ",
        "Rutu" to "ಋತು",
        "Masa" to "ಮಾಸ",
        "Paksha" to "ಪಕ್ಷ",
        "Panchanga" to "ಪಂಚಾಂಗ",
        "Hora Companion" to "ಹೋರ ಕಂಪ್ಯಾನಿಯನ್"
    )

    fun translate(value: String, lang: String, category: String = "ui"): String {
        if (lang != "kn") return value
        // Data translations are now handled by the backend. 
        // We only translate UI labels here.
        return if (category == "ui") {
            UI[value] ?: value
        } else {
            value
        }
    }
}
