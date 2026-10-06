package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.model.SessionWithRound

data class SessionCreateResponse(
    val id: Long,
    val seminarId: Long,
    val round: Int,
) {
    companion object {
        fun from(sessionWithRound: SessionWithRound): SessionCreateResponse =
            SessionCreateResponse(
                id = sessionWithRound.session.id!!,
                seminarId = sessionWithRound.session.seminarId,
                round = sessionWithRound.round,
            )
    }
}
