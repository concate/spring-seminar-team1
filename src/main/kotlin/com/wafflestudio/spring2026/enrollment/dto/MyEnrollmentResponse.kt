package com.wafflestudio.spring2026.enrollment.dto

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import com.wafflestudio.spring2026.seminar.dto.SeminarResponse
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class MyEnrollmentResponse(
    val id: Long,
    val seminar: SeminarResponse,
    val graceDaysRemaining: Int,
    val dropped: Boolean,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun of(
            enrollment: Enrollment,
            seminar: SeminarResponse,
        ) = MyEnrollmentResponse(
            id = enrollment.id!!,
            seminar = seminar,
            graceDaysRemaining = enrollment.graceDaysRemaining,
            dropped = enrollment.dropped,
            createdAt = enrollment.createdAt.atOffset(ZoneOffset.UTC),
        )
    }
}
