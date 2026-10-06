package com.wafflestudio.spring2026.auth

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class UnauthorizedException(
    message: String = "인증이 필요합니다.",
) : ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message)

class ForbiddenException(
    message: String = "권한이 없습니다.",
) : ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message)
