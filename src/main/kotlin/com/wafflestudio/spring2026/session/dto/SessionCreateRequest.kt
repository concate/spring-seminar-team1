package com.wafflestudio.spring2026.session.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

// 필수 항목도 nullable 로 받아, 누락된 경우 역직렬화 오류가 아니라 검증 오류(400)로 처리합니다.
data class SessionCreateRequest(
    @field:NotBlank(message = "회차 제목은 비어 있을 수 없습니다.")
    val title: String? = null,

    @field:NotNull(message = "회차 시작 일시는 필수입니다.")
    val startsAt: OffsetDateTime? = null,

    @field:NotBlank(message = "회차 장소는 비어 있을 수 없습니다.")
    val location: String? = null,

    @field:NotBlank(message = "과제 제목은 비어 있을 수 없습니다.")
    val assignmentTitle: String? = null,

    val lectureContent: String? = null,

    val assignmentContent: String? = null,
)
