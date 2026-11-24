package com.wafflestudio.spring2026.enrollment.controller

import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.auth.LoginUser
import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateResponse
import com.wafflestudio.spring2026.enrollment.dto.EnrollmentUpdateRequest
import com.wafflestudio.spring2026.enrollment.dto.EnrollmentUpdateResponse
import com.wafflestudio.spring2026.enrollment.service.EnrollmentService
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode

@RestController
@RequestMapping("/seminars/{seminarId}/enrollments")
class EnrollmentController(
    private val enrollmentService: EnrollmentService,
) {
    @PostMapping
    fun enroll(
        @PathVariable("seminarId") seminarId: Long,
        @LoginUser user: AuthUser,
    ): ResponseEntity<EnrollmentCreateResponse> {
        val enrollment = enrollmentService.enroll(seminarId, user)

        return ResponseEntity
            .created(URI.create("/seminars/$seminarId/enrollments/${enrollment.id}"))
            .body(EnrollmentCreateResponse.from(enrollment))
    }

    @PatchMapping("/{enrollmentId}")
    fun updateEnrollment(
        @PathVariable("seminarId") seminarId: Long,
        @PathVariable("enrollmentId") enrollmentId: Long,
        @LoginUser user: AuthUser,
        @RequestBody(required = false) body: JsonNode?,
    ): ResponseEntity<EnrollmentUpdateResponse> {
        val enrollment = enrollmentService.updateEnrollment(user, seminarId, enrollmentId, EnrollmentUpdateRequest.parse(body))
        return ResponseEntity.ok(EnrollmentUpdateResponse.from(enrollment))
    }

    @DeleteMapping("/me")
    fun cancel(
        @PathVariable("seminarId") seminarId: Long,
        @LoginUser user: AuthUser,
    ): ResponseEntity<Void> {
        enrollmentService.cancel(seminarId, user)
        return ResponseEntity.noContent().build()
    }
}