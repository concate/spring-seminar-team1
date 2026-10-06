package com.wafflestudio.spring2026.seminar

class SeminarNotFoundException(
    seminarId: Long,
) : RuntimeException(
        "ID가 ${seminarId}인 세미나를 찾을 수 없습니다.",
    )
