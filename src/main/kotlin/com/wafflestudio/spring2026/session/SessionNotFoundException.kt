package com.wafflestudio.spring2026.session

class SessionNotFoundException(
    sessionId: Long,
) : RuntimeException(
        "ID가 ${sessionId}인 회차를 찾을 수 없습니다.",
    )
