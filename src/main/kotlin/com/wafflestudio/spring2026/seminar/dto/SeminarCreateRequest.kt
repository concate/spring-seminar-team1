package com.wafflestudio.spring2026.seminar.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class SeminarCreateRequest(
    @field:NotBlank(message = "세미나 제목은 비어 있을 수 없습니다.")
    val title: String? = null,

    val description: String? = null,

    @field:NotNull(message = "정원은 필수입니다.")
    @field:Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    val capacity: Int? = null,

    @field:NotNull(message = "신청 시작 일시는 필수입니다.")
    val applyStartAt: OffsetDateTime? = null,

    @field:NotNull(message = "신청 종료 일시는 필수입니다.")
    val applyEndAt: OffsetDateTime? = null,

    @field:NotNull(message = "총 Grace Day 는 필수입니다.")
    @field:Min(value = 0, message = "총 Grace Day 는 0 이상이어야 합니다.")
    val totalGraceDays: Int? = null,
)
