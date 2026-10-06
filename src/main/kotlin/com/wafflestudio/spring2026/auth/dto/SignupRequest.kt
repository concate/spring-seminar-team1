package com.wafflestudio.spring2026.auth.dto

import com.wafflestudio.spring2026.user.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SignupRequest(
    @field:NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
    val password: String,

    @field:NotBlank(message = "이름은 비어 있을 수 없습니다.")
    val name: String,

    @field:NotBlank(message = "GitHub 사용자명은 비어 있을 수 없습니다.")
    val githubUsername: String,

    @field:NotNull(message = "역할은 비어 있을 수 없습니다.")
    val role: Role?,

    val seminarId: Long? = null,
)
