package com.wafflestudio.spring2026.seminar.dto

import com.wafflestudio.spring2026.seminar.model.SeminarDetail
import com.wafflestudio.spring2026.seminar.model.SeminarStatus
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SeminarResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val capacity: Int,
    val enrolledCount: Int,
    val applyStartAt: OffsetDateTime,
    val applyEndAt: OffsetDateTime,
    val totalGraceDays: Int,
    val status: SeminarStatus,
    val sessionCount: Int,
) {
    companion object {
        fun from(detail: SeminarDetail): SeminarResponse {
            val seminar = detail.seminar

            return SeminarResponse(
                id = seminar.id!!,
                title = seminar.title,
                description = seminar.description,
                capacity = seminar.capacity,
                enrolledCount = detail.enrolledCount,
                applyStartAt = seminar.applyStartAt.atOffset(ZoneOffset.UTC),
                applyEndAt = seminar.applyEndAt.atOffset(ZoneOffset.UTC),
                totalGraceDays = seminar.totalGraceDays,
                status = detail.status,
                sessionCount = detail.sessionCount,
            )
        }
    }
}
