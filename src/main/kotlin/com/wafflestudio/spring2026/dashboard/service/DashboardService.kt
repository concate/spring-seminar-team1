package com.wafflestudio.spring2026.dashboard.service

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.dashboard.dto.DashboardRecordResponse
import com.wafflestudio.spring2026.dashboard.dto.DashboardResponse
import com.wafflestudio.spring2026.dashboard.dto.DashboardRowResponse
import com.wafflestudio.spring2026.dashboard.dto.DashboardSessionResponse
import com.wafflestudio.spring2026.dashboard.dto.DashboardUserResponse
import com.wafflestudio.spring2026.dashboard.repository.DashboardRepository
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.repository.SessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset

@Service
class DashboardService(
    private val dashboardRepository: DashboardRepository,
    private val seminarRepository: SeminarRepository,
    private val sessionRepository: SessionRepository,
    private val enrollmentRepository: EnrollmentRepository,
) {
    // 회차·수강생·기록을 한 읽기 트랜잭션에서 조회한다.
    @Transactional(readOnly = true)
    fun getDashboard(
        requester: AuthUser,
        seminarId: Long,
        pageQuery: PageQuery,
    ): DashboardResponse {
        if (!seminarRepository.existsById(seminarId)) throw SeminarNotFoundException(seminarId)
        // 와장, 담당 운영진, 이 세미나를 수강 중인 루키(탈락 표시 포함)만 조회할 수 있다.
        requester.requireMemberOf(seminarId) {
            enrollmentRepository.existsBySeminarIdAndRookieId(seminarId, requester.id)
        }

        // 회차 정렬과 round 계산은 기존 회차 목록 조회(GET /seminars/{id}/sessions)와 같은 규칙이다.
        val sessions = sessionRepository
            .findAllBySeminarIdOrderByStartsAtAscIdAsc(seminarId)
            .mapIndexed { index, session ->
                DashboardSessionResponse(
                    id = session.id!!,
                    round = index + 1,
                    title = session.title,
                    startsAt = session.startsAt.atOffset(ZoneOffset.UTC),
                    assignmentTitle = session.assignmentTitle,
                )
            }

        val enrollments = dashboardRepository.findEnrollmentPage(seminarId, pageQuery.size, pageQuery.offset)
        val total = dashboardRepository.countEnrollments(seminarId)

        // 이번 페이지 수강생의 기록만 한 번에 읽고, (수강 신청 ID, 회차 ID) 로 찾는다.
        val recordsByKey = dashboardRepository
            .findRecords(enrollments.map { it.enrollmentId })
            .associateBy { it.enrollmentId to it.sessionId }

        val content = enrollments.map { row ->
            DashboardRowResponse(
                id = row.enrollmentId,
                user = DashboardUserResponse(
                    id = row.userId,
                    email = row.email,
                    name = row.name,
                    githubUsername = row.githubUsername,
                ),
                graceDaysRemaining = row.graceDaysRemaining,
                dropped = row.dropped,
                createdAt = row.createdAt.atOffset(ZoneOffset.UTC),
                // 기록이 없는 회차도 생략하지 않고 null 로 채운다. 순서는 sessions 와 같다.
                records = sessions.map { session ->
                    val record = recordsByKey[row.enrollmentId to session.id]
                    DashboardRecordResponse(
                        sessionId = session.id,
                        attendance = record?.attendance,
                        assignmentStatus = record?.assignmentStatus,
                    )
                },
            )
        }

        val page = pageQuery.toResponse(content, total)
        return DashboardResponse(
            seminarId = seminarId,
            sessions = sessions,
            content = page.content,
            page = page.page,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}
