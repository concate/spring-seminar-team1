package com.wafflestudio.spring2026

import com.wafflestudio.spring2026.meeting.MeetingNotFoundException
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.session.SessionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = exception.bindingResult.fieldErrors.map { error ->
            FieldErrorResponse(
                field = error.field,
                message = error.defaultMessage ?: "Invalid value.",
            )
        }

        return ResponseEntity.badRequest().body(
            ApiErrorResponse(
                code = "INVALID_REQUEST",
                message = "Invalid request.",
                fieldErrors = fieldErrors,
            ),
        )
    }

    @ExceptionHandler(MeetingNotFoundException::class)
    fun handleMeetingNotFound(
        exception: MeetingNotFoundException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "MEETING_NOT_FOUND",
                message = exception.message ?: "Meeting not found.",
            ),
        )

    @ExceptionHandler(SeminarNotFoundException::class)
    fun handleSeminarNotFound(
        exception: SeminarNotFoundException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "SEMINAR_NOT_FOUND",
                message = exception.message ?: "Seminar not found.",
            ),
        )

    @ExceptionHandler(SessionNotFoundException::class)
    fun handleSessionNotFound(
        exception: SessionNotFoundException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "SESSION_NOT_FOUND",
                message = exception.message ?: "Session not found.",
            ),
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        exception: HttpMessageNotReadableException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.badRequest().body(
            ApiErrorResponse(
                code = "INVALID_REQUEST",
                message = "Could not read the request body.",
            ),
        )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        exception: MethodArgumentTypeMismatchException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.badRequest().body(
            ApiErrorResponse(
                code = "INVALID_REQUEST",
                message = "Invalid value for ${exception.name}.",
            ),
        )

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(exception.status).body(
            ApiErrorResponse(
                code = exception.code,
                message = exception.message ?: exception.status.reasonPhrase,
            ),
        )
}
