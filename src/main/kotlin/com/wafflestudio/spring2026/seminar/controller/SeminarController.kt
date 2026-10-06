package com.wafflestudio.spring2026.seminar.controller

import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarUpdateRequest
import com.wafflestudio.spring2026.seminar.service.SeminarService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seminars")
class SeminarController(
    private val seminarService: SeminarService,
) {
    @PostMapping
    fun createSeminar(
        @Valid @RequestBody request: SeminarCreateRequest,
    ): ResponseEntity<SeminarCreateResponse> {
        val seminar = seminarService.createSeminar(
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

    @GetMapping("/{seminarId}")
    fun getSeminar(
        @PathVariable("seminarId") seminarId: Long,
    ): ResponseEntity<SeminarResponse> = ResponseEntity.ok(SeminarResponse.from(seminarService.getSeminar(seminarId)))

    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @PathVariable("seminarId") seminarId: Long,
        @RequestBody request: SeminarUpdateRequest,
    ): ResponseEntity<SeminarResponse> {
        val detail = seminarService.updateSeminar(
            seminarId = seminarId,
            title = request.title,
            description = request.description,
        )

        return ResponseEntity.ok(SeminarResponse.from(detail))
    }
}
