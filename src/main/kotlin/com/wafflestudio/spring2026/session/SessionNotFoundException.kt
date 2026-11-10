package com.wafflestudio.spring2026.session

class SessionNotFoundException(
    sessionId: Long,
) : RuntimeException(
        "Session $sessionId not found.",
    )
