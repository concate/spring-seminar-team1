package com.wafflestudio.spring2026.session.dto

import java.time.OffsetDateTime
import org.openapitools.jackson.nullable.JsonNullable

data class SessionUpdateRequest(
    val title: JsonNullable<String?> = JsonNullable.undefined(),
    val startsAt: JsonNullable<OffsetDateTime?> = JsonNullable.undefined(),
    val location: JsonNullable<String?> = JsonNullable.undefined(),
    val assignmentTitle: JsonNullable<String?> = JsonNullable.undefined(),
    val lectureContent: JsonNullable<String?> = JsonNullable.undefined(),
    val assignmentContent: JsonNullable<String?> = JsonNullable.undefined(),
)
