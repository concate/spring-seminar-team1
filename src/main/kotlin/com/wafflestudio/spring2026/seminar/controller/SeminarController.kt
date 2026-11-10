package com.wafflestudio.spring2026.seminar.controller

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.PageResponse
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.auth.LoginUser
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarUpdateRequest
import com.wafflestudio.spring2026.seminar.model.SeminarStatus
import com.wafflestudio.spring2026.seminar.service.SeminarService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/seminars")
class SeminarController(
    private val seminarService: SeminarService,
) {
    @PostMapping
    fun createSeminar(
        @LoginUser user: AuthUser,
        @Valid @RequestBody request: SeminarCreateRequest,
    ): ResponseEntity<SeminarCreateResponse> {
        val seminar = seminarService.createSeminar(
            requester = user,
            title = request.title!!,
            description = request.description,
            capacity = request.capacity!!,
            applyStartAt = request.applyStartAt!!,
            applyEndAt = request.applyEndAt!!,
            totalGraceDays = request.totalGraceDays!!,
        )

        return ResponseEntity
            .created(URI.create("/seminars/${seminar.id}"))
            .body(SeminarCreateResponse.from(seminar))
    }

    @GetMapping
    fun searchSeminars(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) status: SeminarStatus?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ResponseEntity<PageResponse<SeminarResponse>> =
        ResponseEntity.ok(
            seminarService
                .searchSeminars(keyword, status, PageQuery.of(page, size))
                .map(SeminarResponse::from),
        )

    @GetMapping("/{seminarId}")
    fun getSeminar(
        @LoginUser user: AuthUser,
        @PathVariable("seminarId") seminarId: Long,
    ): ResponseEntity<SeminarResponse> =
        ResponseEntity.ok(SeminarResponse.from(seminarService.getSeminar(user, seminarId)))

    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @LoginUser user: AuthUser,
        @PathVariable("seminarId") seminarId: Long,
        @RequestBody request: SeminarUpdateRequest,
    ): ResponseEntity<SeminarResponse> {
        val detail = seminarService.updateSeminar(
            requester = user,
            seminarId = seminarId,
            title = request.title,
            description = request.description,
        )

        return ResponseEntity.ok(SeminarResponse.from(detail))
    }
}
