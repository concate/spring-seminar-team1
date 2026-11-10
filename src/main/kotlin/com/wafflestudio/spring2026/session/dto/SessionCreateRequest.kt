package com.wafflestudio.spring2026.session.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

// 필수 항목도 nullable 로 받아, 누락된 경우 역직렬화 오류가 아니라 검증 오류(400)로 처리합니다.
data class SessionCreateRequest(
    @field:NotBlank(message = "Session title must not be blank.")
    val title: String? = null,

    @field:NotNull(message = "Session start time is required.")
    val startsAt: OffsetDateTime? = null,

    @field:NotBlank(message = "Session location must not be blank.")
    val location: String? = null,

    @field:NotBlank(message = "Assignment title must not be blank.")
    val assignmentTitle: String? = null,

    val lectureContent: String? = null,

    val assignmentContent: String? = null,
)
