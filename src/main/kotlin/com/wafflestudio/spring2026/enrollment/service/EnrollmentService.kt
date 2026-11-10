package com.wafflestudio.spring2026.enrollment.service

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.PageResponse
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.enrollment.AlreadyEnrolledException
import com.wafflestudio.spring2026.enrollment.EnrollmentNotFoundException
import com.wafflestudio.spring2026.enrollment.NotInApplyPeriodException
import com.wafflestudio.spring2026.enrollment.SeminarNotOpenException
import com.wafflestudio.spring2026.enrollment.dto.MyEnrollmentResponse
import com.wafflestudio.spring2026.enrollment.model.Enrollment
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.dto.SeminarResponse
import com.wafflestudio.spring2026.seminar.model.SeminarDetail
import com.wafflestudio.spring2026.seminar.model.SeminarStatus
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EnrollmentService(
    private val enrollmentRepository: EnrollmentRepository,
    private val seminarRepository: SeminarRepository,
) {
    @Transactional
    fun enroll(
        seminarId: Long,
        user: AuthUser,
    ): Enrollment {
        user.requireRookie()

        val seminar = seminarRepository.findByIdForUpdate(seminarId)
            ?: throw SeminarNotFoundException(seminarId)

        if (enrollmentRepository.existsBySeminarIdAndRookieId(seminarId, user.id)) {
            throw AlreadyEnrolledException()
        }

        val now = nowUtc()
        if (seminar.statusAt(now, seminarRepository.countEnrollments(seminarId)) != SeminarStatus.OPEN) {
            throw SeminarNotOpenException()
        }

        return enrollmentRepository.save(
            Enrollment(
                seminarId = seminarId,
                rookieId = user.id,
                graceDaysRemaining = seminar.totalGraceDays,
                createdAt = now,
            ),
        )
    }

    @Transactional
    fun cancel(
        seminarId: Long,
        user: AuthUser,
    ) {
        user.requireRookie() // 운영진 → 403

        // 세미나 행을 먼저 잠가 같은 사용자의 동시 취소를 직렬화한다 (중복 취소 → 404)
        val seminar = seminarRepository.findByIdForUpdate(seminarId)
            ?: throw SeminarNotFoundException(seminarId)
        val enrollment = enrollmentRepository.findBySeminarIdAndRookieId(seminarId, user.id)
            ?: throw EnrollmentNotFoundException(seminarId)

        if (!seminar.isApplyPeriod(nowUtc())) throw NotInApplyPeriodException()

        enrollmentRepository.delete(enrollment)
    }

    @Transactional(readOnly = true)
    fun getMyEnrollments(
        user: AuthUser,
        pageQuery: PageQuery,
    ): PageResponse<MyEnrollmentResponse> {
        user.requireRookie() // 운영진·미승인·반려 → 403

        val enrollments = enrollmentRepository.findPageByRookieId(user.id, pageQuery.size, pageQuery.offset)
        val total = enrollmentRepository.countByRookieId(user.id)
        val now = nowUtc()

        // 항목마다 세미나 조회와 COUNT 를 실행한다(N+1). size 최대 100 이라 3주차 범위에선 충분하다.
        val content = enrollments.map { enrollment ->
            val seminar = seminarRepository.findByIdOrNull(enrollment.seminarId)
                ?: throw SeminarNotFoundException(enrollment.seminarId)
            val enrolledCount = seminarRepository.countEnrollments(enrollment.seminarId)
            val detail = SeminarDetail(
                seminar = seminar,
                enrolledCount = enrolledCount,
                sessionCount = seminarRepository.countSessions(enrollment.seminarId),
                status = seminar.statusAt(now, enrolledCount),
            )
            MyEnrollmentResponse.of(enrollment, SeminarResponse.from(detail))
        }

        return pageQuery.toResponse(content, total)
    }

    private fun nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}