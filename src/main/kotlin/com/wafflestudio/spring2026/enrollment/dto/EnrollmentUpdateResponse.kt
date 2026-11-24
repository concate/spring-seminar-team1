package com.wafflestudio.spring2026.enrollment.dto

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class EnrollmentUpdateResponse(
    val id: Long,
    val graceDaysRemaining: Int,
    val dropped: Boolean,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(enrollment: Enrollment) =
            EnrollmentUpdateResponse(
                id = enrollment.id!!,
                graceDaysRemaining = enrollment.graceDaysRemaining,
                dropped = enrollment.dropped,
                createdAt = enrollment.createdAt.atOffset(ZoneOffset.UTC),
            )
    }
}
