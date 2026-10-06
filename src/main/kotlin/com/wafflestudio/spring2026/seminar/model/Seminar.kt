package com.wafflestudio.spring2026.seminar.model

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("seminar")
data class Seminar(
    @Id
    val id: Long? = null,
    val title: String,
    val description: String?,
    val capacity: Int,
    val applyStartAt: LocalDateTime,
    val applyEndAt: LocalDateTime,
    val totalGraceDays: Int,
    val createdAt: LocalDateTime,
) {
    fun statusAt(
        now: LocalDateTime,
        enrolledCount: Int,
    ): SeminarStatus =
        when {
            !now.isBefore(applyEndAt) || enrolledCount >= capacity -> SeminarStatus.CLOSED
            now.isBefore(applyStartAt) -> SeminarStatus.BEFORE
            else -> SeminarStatus.OPEN
        }

    fun isApplyPeriod(now: LocalDateTime): Boolean = !now.isBefore(applyStartAt) && now.isBefore(applyEndAt)
}

data class SeminarDetail(
    val seminar: Seminar,
    val enrolledCount: Int,
    val sessionCount: Int,
    val status: SeminarStatus,
)
