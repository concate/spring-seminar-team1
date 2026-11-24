package com.wafflestudio.spring2026.dashboard.controller

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.auth.LoginUser
import com.wafflestudio.spring2026.dashboard.dto.DashboardResponse
import com.wafflestudio.spring2026.dashboard.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController(
    private val dashboardService: DashboardService,
) {
    // page·size 가 정수가 아니면 MethodArgumentTypeMismatchException → 400, 범위 밖이면 PageQuery.of 에서 400.
    @GetMapping("/seminars/{seminarId}/dashboard")
    fun getDashboard(
        @LoginUser user: AuthUser,
        @PathVariable("seminarId") seminarId: Long,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ResponseEntity<DashboardResponse> =
        ResponseEntity.ok(dashboardService.getDashboard(user, seminarId, PageQuery.of(page, size)))
}
