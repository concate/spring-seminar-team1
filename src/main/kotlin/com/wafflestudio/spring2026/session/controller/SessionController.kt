package com.wafflestudio.spring2026.session.controller

import com.wafflestudio.spring2026.session.dto.SessionCreateRequest
import com.wafflestudio.spring2026.session.dto.SessionCreateResponse
import com.wafflestudio.spring2026.session.dto.SessionDetailResponse
import com.wafflestudio.spring2026.session.dto.SessionSummaryResponse
import com.wafflestudio.spring2026.session.service.SessionService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SessionController(
    private val sessionService: SessionService,
) {
    @PostMapping("/seminars/{seminarId}/sessions")
    fun createSession(
        @PathVariable("seminarId") seminarId: Long,
        @Valid @RequestBody request: SessionCreateRequest,
    ): ResponseEntity<SessionCreateResponse> {
        // @Valid 로 필수 항목이 검증되었으므로 non-null 입니다.
        val session = sessionService.createSession(
            seminarId = seminarId,
            title = request.title!!,
            startsAt = request.startsAt!!,
            location = request.location!!,
            assignmentTitle = request.assignmentTitle!!,
            lectureContent = request.lectureContent,
            assignmentContent = request.assignmentContent,
        )

        val response = SessionCreateResponse.from(session)

        return ResponseEntity
            .created(URI.create("/sessions/${response.id}"))
            .body(response)
    }

    @GetMapping("/seminars/{seminarId}/sessions")
    fun getSessions(
        @PathVariable("seminarId") seminarId: Long,
    ): ResponseEntity<List<SessionSummaryResponse>> =
        ResponseEntity.ok(
            sessionService.getSessions(seminarId).map(SessionSummaryResponse::from),
        )

    @GetMapping("/sessions/{sessionId}")
    fun getSession(
        @PathVariable("sessionId") sessionId: Long,
    ): ResponseEntity<SessionDetailResponse> =
        ResponseEntity.ok(
            SessionDetailResponse.from(sessionService.getSession(sessionId)),
        )
}
