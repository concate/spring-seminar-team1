package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.model.SessionWithRound
import java.time.OffsetDateTime
import java.time.ZoneOffset

// 회차 목록의 항목. 설명 본문(lectureContent, assignmentContent)은 담지 않습니다.
data class SessionSummaryResponse(
    val id: Long,
    val seminarId: Long,
    val round: Int,
    val title: String,
    val startsAt: OffsetDateTime,
    val location: String,
    val assignmentTitle: String,
) {
    companion object {
        fun from(sessionWithRound: SessionWithRound): SessionSummaryResponse {
            val session = sessionWithRound.session

            return SessionSummaryResponse(
                id = session.id!!,
                seminarId = session.seminarId,
                round = sessionWithRound.round,
                title = session.title,
                startsAt = session.startsAt.atOffset(ZoneOffset.UTC),
                location = session.location,
                assignmentTitle = session.assignmentTitle,
            )
        }
    }
}
