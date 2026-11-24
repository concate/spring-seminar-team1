package com.wafflestudio.spring2026.session

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class InvalidRecordsRequestException(
    message: String,
) : ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message)

class RecordEnrollmentNotFoundException(
    enrollmentId: Long,
) : ApiException(HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "Enrollment $enrollmentId not found in this seminar.")
