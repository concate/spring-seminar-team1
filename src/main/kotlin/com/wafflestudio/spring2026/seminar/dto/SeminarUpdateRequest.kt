package com.wafflestudio.spring2026.seminar.dto

import org.openapitools.jackson.nullable.JsonNullable

data class SeminarUpdateRequest(
    val title: JsonNullable<String?> = JsonNullable.undefined(),
    val description: JsonNullable<String?> = JsonNullable.undefined(),
)
