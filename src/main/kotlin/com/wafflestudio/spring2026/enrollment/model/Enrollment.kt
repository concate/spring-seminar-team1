package com.wafflestudio.spring2026.enrollment.model

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("enrollment")
data class Enrollment(
    @Id
    val id: Long? = null,
    val seminarId: Long,
    val rookieId: Long,
    val graceDaysRemaining: Int,
    val dropped: Boolean = false,
    val createdAt: LocalDateTime,
)
