package com.hora.companion.utils

object TranslationUtils {
    private val PLANETS = mapOf(
        "Sun" to "ಸೂರ್ಯ",
        "Moon" to "ಚಂದ್ರ",
        "Mars" to "ಕುಜ",
        "Mercury" to "ಬುಧ",
        "Jupiter" to "ಗುರು",
        "Venus" to "ಶುಕ್ರ",
        "Saturn" to "ಶನಿ"
    )

    private val RASI = mapOf(
        "Mesha" to "ಮೇಷ",
        "Vrishabha" to "ವೃಷಭ",
        "Mithuna" to "ಮಿಥುನ",
        "Cancer" to "ಕಟಕ",
        "Leo" to "ಸಿಂಹ",
        "Virgo" to "ಕನ್ಯಾ",
        "Libra" to "ತುಲಾ",
        "Scorpio" to "ವೃಶ್ಚಿಕ",
        "Sagittarius" to "ಧನು",
        "Capricorn" to "ಮಕರ",
        "Aquarius" to "ಕುಂಭ",
        "Pisces" to "ಮೀನ"
    )

    private val NAKSHATRA = mapOf(
        "Ashwini" to "ಅಶ್ವಿನಿ",
        "Bharani" to "ಭರಣಿ",
        "Krittika" to "ಕೃತ್ತಿಕಾ",
        "Rohini" to "ರೋಹಿಣಿ",
        "Mrigashira" to "ಮೃಗಶಿರ",
        "Ardra" to "ಆರ್ದ್ರ",
        "Punarvasu" to "ಪುನರ್ವಸು",
        "Pushya" to "ಪುಷ್ಯ",
        "Ashlesha" to "ಆಶ್ಲೇಷ",
        "Ashlesa" to "ಆಶ್ಲೇಷ",
        "Magha" to "ಮಘಾ",
        "Purva Phalguni" to "ಪೂರ್ವ ಫಲ್ಗುಣಿ",
        "Uttara Phalguni" to "ಉತ್ತರ ಫಲ್ಗುಣಿ",
        "Hasta" to "ಹಸ್ತ",
        "Chitra" to "ಚಿತ್ರಾ",
        "Swati" to "ಸ್ವಾತಿ",
        "Vishakha" to "ವಿಶಾಖ",
        "Anuradha" to "ಅನುರಾಧ",
        "Jyeshtha" to "ಜ್ಯೇಷ್ಠ",
        "Mula" to "ಮೂಲ",
        "Purva Ashadha" to "ಪೂರ್ವಾಷಾಢ",
        "Uttara Ashadha" to "ಉತ್ತರಾಷಾಢ",
        "Shravana" to "ಶ್ರವಣ",
        "Dhanishta" to "ಧನಿಷ್ಠ",
        "Shatabhisha" to "ಶತಭಿಷ",
        "Purva Bhadrapada" to "ಪೂರ್ವ ಭಾದ್ರಪದ",
        "Uttara Bhadrapada" to "ಉತ್ತರ ಭಾದ್ರಪದ",
        "Revati" to "ರೇವತಿ"
    )

    private val TITHI = mapOf(
        "Krishna" to "ಕೃಷ್ಣ",
        "Shukla" to "ಶುಕ್ಲ",
        "Pratipada" to "ಪ್ರತಿಪದೆ",
        "Dwitiya" to "ದ್ವಿತೀಯ",
        "Tritiya" to "ತೃತೀಯ",
        "Chaturthi" to "ಚತುರ್ಥಿ",
        "Panchami" to "ಪಂಚಮಿ",
        "Shashthi" to "ಷಷ್ಠಿ",
        "Saptami" to "ಸಪ್ತಮಿ",
        "Ashtami" to "ಅಷ್ಟಮಿ",
        "Navami" to "ನವಮಿ",
        "Dashami" to "ದಶಮಿ",
        "Ekadashi" to "ಏಕಾದಶಿ",
        "Dwadashi" to "ದ್ವಾದಶಿ",
        "Trayodashi" to "ತ್ರಯೋದಶಿ",
        "Chaturdashi" to "ಚತುರ್ದಶಿ",
        "Purnima" to "ಪೌರ್ಣಮಿ",
        "Amavasya" to "ಅಮಾವಾಸ್ಯೆ"
    )

    private val VARA = mapOf(
        "Sunday" to "ಭಾನುವಾರ",
        "Monday" to "ಸೋಮವಾರ",
        "Tuesday" to "ಮಂಗಳವಾರ",
        "Wednesday" to "ಬುಧವಾರ",
        "Thursday" to "ಗುರುವಾರ",
        "Friday" to "ಶುಕ್ರವಾರ",
        "Saturday" to "ಶನಿವಾರ"
    )

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
        "Hora" to "ಹೋರೆ"
    )

    fun translate(value: String, lang: String, category: String = "ui"): String {
        if (lang != "kn") return value
        val map = when (category) {
            "planet" -> PLANETS
            "rasi" -> RASI
            "nakshatra" -> NAKSHATRA
            "tithi" -> TITHI
            "vara" -> VARA
            "ui" -> UI
            else -> emptyMap()
        }
        
        return if (category == "tithi") {
            value.split(" ").joinToString(" ") { map[it] ?: it }
        } else {
            map[value] ?: value
        }
    }
}
