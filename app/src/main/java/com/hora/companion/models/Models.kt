package com.hora.companion.models

// The backend JSON shapes may evolve. Use general-purpose types or
// add stricter models after inspecting real responses.

data class AllResponse(
    val data: Map<String, Any> = emptyMap()
)

data class KundaliResponse(
    val data: Map<String, Any> = emptyMap()
)
