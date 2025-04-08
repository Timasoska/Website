package com.example.model

import kotlinx.serialization.Serializable

@Serializable data class RegisterRequest( val login: String, val email: String, val password: String )
@Serializable data class LoginRequest( val login: String, val password: String )
@Serializable data class AuthResponse( val success: Boolean, val message: String )
@Serializable data class ErrorResponse( val message: String )