package com.wafflestudio.spring2026.seminar.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class SeminarCreateRequest(
    @field:NotBlank(message = "Seminar title must not be blank.")
    val title: String? = null,

    val description: String? = null,

    @field:NotNull(message = "Capacity is required.")
    @field:Min(value = 1, message = "Capacity must be at least 1.")
    val capacity: Int? = null,

    @field:NotNull(message = "Application start time is required.")
    val applyStartAt: OffsetDateTime? = null,

    @field:NotNull(message = "Application end time is required.")
    val applyEndAt: OffsetDateTime? = null,

    @field:NotNull(message = "Total grace days is required.")
    @field:Min(value = 0, message = "Total grace days must be at least 0.")
    val totalGraceDays: Int? = null,
)
