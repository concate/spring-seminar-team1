package com.wafflestudio.spring2026.enrollment.dto

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class EnrollmentCreateResponse(
    val id: Long,
    val graceDaysRemaining: Int,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(enrollment: Enrollment) =
            EnrollmentCreateResponse(
                id = enrollment.id!!,
                graceDaysRemaining = enrollment.graceDaysRemaining,
                createdAt = enrollment.createdAt.atOffset(ZoneOffset.UTC),
            )
    }
}
