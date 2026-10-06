package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.model.SessionWithRound
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SessionDetailResponse(
    val id: Long,
    val seminarId: Long,
    val round: Int,
    val title: String,
    val startsAt: OffsetDateTime,
    val location: String,
    val assignmentTitle: String,
    val lectureContent: String?,
    val assignmentContent: String?,
) {
    companion object {
        fun from(sessionWithRound: SessionWithRound): SessionDetailResponse {
            val session = sessionWithRound.session

            return SessionDetailResponse(
                id = session.id!!,
                seminarId = session.seminarId,
                round = sessionWithRound.round,
                title = session.title,
                startsAt = session.startsAt.atOffset(ZoneOffset.UTC),
                location = session.location,
                assignmentTitle = session.assignmentTitle,
                lectureContent = session.lectureContent,
                assignmentContent = session.assignmentContent,
            )
        }
    }
}
