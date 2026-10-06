package com.wafflestudio.spring2026

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    val code: String,
    message: String,
) : RuntimeException(message)
