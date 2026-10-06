package com.wafflestudio.spring2026.enrollment.controller

import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateRequest
import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateResponse
import com.wafflestudio.spring2026.enrollment.service.EnrollmentService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seminars/{seminarId}/enrollments")
class EnrollmentController(
    private val enrollmentService: EnrollmentService,
) {
    @PostMapping
    fun enroll(
        @PathVariable("seminarId") seminarId: Long,
        @Valid @RequestBody request: EnrollmentCreateRequest,
    ): ResponseEntity<EnrollmentCreateResponse> {
        val enrollment = enrollmentService.enroll(
            seminarId = seminarId,
            rookieId = request.rookieId!!,
        )

        return ResponseEntity
            .created(URI.create("/seminars/$seminarId/enrollments/${enrollment.id}"))
            .body(EnrollmentCreateResponse.from(enrollment))
    }

    @DeleteMapping("/{enrollmentId}")
    fun cancel(
        @PathVariable("seminarId") seminarId: Long,
        @PathVariable("enrollmentId") enrollmentId: Long,
    ): ResponseEntity<Void> {
        enrollmentService.cancel(
            seminarId = seminarId,
            enrollmentId = enrollmentId,
        )

        return ResponseEntity.noContent().build()
    }
}
