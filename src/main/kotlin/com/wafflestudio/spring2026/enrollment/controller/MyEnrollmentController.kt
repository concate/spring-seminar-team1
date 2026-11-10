package com.wafflestudio.spring2026.enrollment.controller

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.PageResponse
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.auth.LoginUser
import com.wafflestudio.spring2026.enrollment.dto.MyEnrollmentResponse
import com.wafflestudio.spring2026.enrollment.service.EnrollmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// 경로는 /users 아래지만 UserController(A 담당)와 충돌하지 않도록 수강 패키지에 둔다.
@RestController
class MyEnrollmentController(
    private val enrollmentService: EnrollmentService,
) {
    @GetMapping("/users/me/enrollments")
    fun getMyEnrollments(
        @LoginUser user: AuthUser,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ResponseEntity<PageResponse<MyEnrollmentResponse>> =
        ResponseEntity.ok(enrollmentService.getMyEnrollments(user, PageQuery.of(page, size)))
}
