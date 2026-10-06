package com.wafflestudio.spring2026.session.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

// startsAt 은 UTC 기준 LocalDateTime 으로 저장합니다.
@Table("session")
data class Session(
    @Id
    val id: Long? = null,
    val seminarId: Long,
    val title: String,
    val startsAt: LocalDateTime,
    val location: String,
    val lectureContent: String?,
    val assignmentTitle: String,
    val assignmentContent: String?,
)

// round 는 저장하지 않고 조회할 때 계산합니다.
data class SessionWithRound(
    val session: Session,
    val round: Int,
)
