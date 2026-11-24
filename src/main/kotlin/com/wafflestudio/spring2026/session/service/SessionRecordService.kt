package com.wafflestudio.spring2026.session.service

import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.RecordEnrollmentNotFoundException
import com.wafflestudio.spring2026.session.SessionNotFoundException
import com.wafflestudio.spring2026.session.model.RecordChange
import com.wafflestudio.spring2026.session.repository.SessionRecordRepository
import com.wafflestudio.spring2026.session.repository.SessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionRecordService(
    private val sessionRepository: SessionRepository,
    private val seminarRepository: SeminarRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val sessionRecordRepository: SessionRecordRepository,
) {
    @Transactional
    fun updateRecords(
        requester: AuthUser,
        sessionId: Long,
        changes: List<RecordChange>,
    ) {
        val session = sessionRepository.findByIdOrNull(sessionId)
            ?: throw SessionNotFoundException(sessionId)
        requester.requireStaffOf(session.seminarId)

        seminarRepository.findByIdForUpdate(session.seminarId)
            ?: throw SeminarNotFoundException(session.seminarId)

        val enrollmentIds = changes.map { it.enrollmentId }
        val validIds = enrollmentRepository
            .findAllById(enrollmentIds)
            .filter { it.seminarId == session.seminarId }
            .mapNotNull { it.id }
            .toSet()
        enrollmentIds.firstOrNull { it !in validIds }?.let { throw RecordEnrollmentNotFoundException(it) }

        changes.forEach { change ->
            sessionRecordRepository.upsert(
                sessionId = sessionId,
                enrollmentId = change.enrollmentId,
                attendanceSet = change.attendance.isPresent,
                attendance = change.attendance.orElse(null)?.name,
                assignmentStatusSet = change.assignmentStatus.isPresent,
                assignmentStatus = change.assignmentStatus.orElse(null)?.name,
            )
            sessionRecordRepository.deleteIfEmpty(sessionId, change.enrollmentId)
        }
    }
}
