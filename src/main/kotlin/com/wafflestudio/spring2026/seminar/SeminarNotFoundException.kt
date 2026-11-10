package com.wafflestudio.spring2026.seminar

class SeminarNotFoundException(
    seminarId: Long,
) : RuntimeException(
    "Seminar $seminarId not found.",
)
