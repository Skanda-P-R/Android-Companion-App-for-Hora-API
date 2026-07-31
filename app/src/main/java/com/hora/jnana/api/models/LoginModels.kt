package com.hora.jnana.api.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val device_uuid: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val error: ApiError
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)
