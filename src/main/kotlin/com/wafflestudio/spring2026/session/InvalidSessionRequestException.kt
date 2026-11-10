package com.wafflestudio.spring2026.session

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class InvalidSessionRequestException(
    message: String,
) : ApiException(HttpStatus.BAD_REQUEST, "INVALID_SESSION_REQUEST", message)
