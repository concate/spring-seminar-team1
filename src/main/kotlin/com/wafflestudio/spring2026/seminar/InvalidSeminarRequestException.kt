package com.wafflestudio.spring2026.seminar

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class InvalidSeminarRequestException(
    message: String,
) : ApiException(HttpStatus.BAD_REQUEST, "INVALID_SEMINAR_REQUEST", message)
