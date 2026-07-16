package com.hora.companion.ui.screens

data class PanchangaState(
    val hora: String = "--",
    val horaSymbol: String = "",
    val horaNext: String = "--",
    val horaEnds: String = "--",
    val remaining: String = "--",
    val tithi: String = "--",
    val nakshatra: String = "--",
    val yoga: String = "--",
    val karana: String = "--",
    val vara: String = "--",
    val samvatsara: String = "--",
    val ayana: String = "--",
    val rutu: String = "--",
    val masa: String = "--",
    val paksha: String = "--",
    val rahuKalam: String = "--",
    val yamaganda: String = "--",
    val abhijit: String = "--",
    val sunrise: String = "--",
    val sunset: String = "--",
    val moonRasi: String = "--",
    val sunRasi: String = "--",
    val lastUpdated: String = "--",
    val isLoading: Boolean = false,
    val error: String? = null
)
