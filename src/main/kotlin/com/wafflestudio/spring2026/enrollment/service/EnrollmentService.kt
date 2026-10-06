package com.wafflestudio.spring2026.enrollment.service

import com.wafflestudio.spring2026.enrollment.AlreadyEnrolledException
import com.wafflestudio.spring2026.enrollment.EnrollmentNotFoundException
import com.wafflestudio.spring2026.enrollment.NotInApplyPeriodException
import com.wafflestudio.spring2026.enrollment.NotRookieException
import com.wafflestudio.spring2026.enrollment.RookieNotApprovedException
import com.wafflestudio.spring2026.enrollment.SeminarNotOpenException
import com.wafflestudio.spring2026.enrollment.model.Enrollment
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.model.SeminarStatus
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.user.UserNotFoundException
import com.wafflestudio.spring2026.user.model.Role
import com.wafflestudio.spring2026.user.model.UserStatus
import com.wafflestudio.spring2026.user.repository.UserRepository
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
    private val userRepository: UserRepository,
) {
    @Transactional
    fun enroll(
        seminarId: Long,
        rookieId: Long,
    ): Enrollment {
        val seminar = seminarRepository.findByIdForUpdate(seminarId)
            ?: throw SeminarNotFoundException(seminarId)
        val rookie = userRepository.findByIdOrNull(rookieId)
            ?: throw UserNotFoundException(rookieId)

        if (rookie.role != Role.ROOKIE) throw NotRookieException()
        if (rookie.status != UserStatus.APPROVED) throw RookieNotApprovedException()
        if (enrollmentRepository.existsBySeminarIdAndRookieId(seminarId, rookieId)) throw AlreadyEnrolledException()

        val now = nowUtc()
        if (seminar.statusAt(now, seminarRepository.countEnrollments(seminarId)) != SeminarStatus.OPEN) {
            throw SeminarNotOpenException()
        }

        return enrollmentRepository.save(
            Enrollment(
                seminarId = seminarId,
                rookieId = rookieId,
                graceDaysRemaining = seminar.totalGraceDays,
                createdAt = now,
            ),
        )
    }

    @Transactional
    fun cancel(
        seminarId: Long,
        enrollmentId: Long,
    ) {
        val enrollment = enrollmentRepository.findByIdAndSeminarId(enrollmentId, seminarId)
            ?: throw EnrollmentNotFoundException(enrollmentId)
        val seminar = seminarRepository.findByIdOrNull(seminarId)
            ?: throw SeminarNotFoundException(seminarId)

        if (!seminar.isApplyPeriod(nowUtc())) throw NotInApplyPeriodException()

        enrollmentRepository.delete(enrollment)
    }

    private fun nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
