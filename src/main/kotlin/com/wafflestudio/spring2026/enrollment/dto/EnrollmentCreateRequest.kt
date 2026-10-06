package com.wafflestudio.spring2026.enrollment.dto

import jakarta.validation.constraints.NotNull

data class EnrollmentCreateRequest(
    @field:NotNull(message = "rookieId 는 필수입니다.")
    val rookieId: Long? = null,
)
