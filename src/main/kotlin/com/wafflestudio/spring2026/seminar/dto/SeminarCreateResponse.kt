package com.wafflestudio.spring2026.seminar.dto

import com.wafflestudio.spring2026.seminar.model.Seminar
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SeminarCreateResponse(
    val id: Long,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(seminar: Seminar) =
            SeminarCreateResponse(
                id = seminar.id!!,
                createdAt = seminar.createdAt.atOffset(ZoneOffset.UTC),
            )
    }
}
