package com.wafflestudio.spring2026.auth.controller

import com.wafflestudio.spring2026.auth.dto.LoginRequest
import com.wafflestudio.spring2026.auth.dto.LoginResponse
import com.wafflestudio.spring2026.auth.dto.SignupRequest
import com.wafflestudio.spring2026.auth.dto.SignupResponse
import com.wafflestudio.spring2026.auth.service.AuthService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<SignupResponse> {
        val user = authService.signup(request)

        return ResponseEntity
            .created(URI.create("/users/${user.id}"))
            .body(SignupResponse.from(user))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val accessToken = authService.login(
            email = request.email,
            password = request.password,
        )

        return ResponseEntity.ok(LoginResponse(accessToken))
    }

    // 인터셉터를 켜지 않아도 동작하도록 헤더를 직접 받아 검증한다.
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): ResponseEntity<Void> {
        authService.logout(authorization)

        return ResponseEntity.noContent().build()
    }
}
