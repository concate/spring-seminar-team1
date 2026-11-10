package com.wafflestudio.spring2026.session.service

import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.InvalidSessionRequestException
import com.wafflestudio.spring2026.session.SessionNotFoundException
import com.wafflestudio.spring2026.session.dto.SessionUpdateRequest
import com.wafflestudio.spring2026.session.model.Session
import com.wafflestudio.spring2026.session.model.SessionWithRound
import com.wafflestudio.spring2026.session.repository.SessionRepository
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val seminarRepository: SeminarRepository,
    private val enrollmentRepository: EnrollmentRepository,
) {
    @Transactional
    fun createSession(
        requester: AuthUser,
        seminarId: Long,
        title: String,
        startsAt: OffsetDateTime,
        location: String,
        assignmentTitle: String,
        lectureContent: String?,
        assignmentContent: String?,
    ): SessionWithRound {
        requester.requireStaffOf(seminarId)
        checkSeminarExists(seminarId)

        val session = sessionRepository.save(
            Session(
                seminarId = seminarId,
                title = title,
                startsAt = startsAt.toUtc(),
                location = location,
                lectureContent = lectureContent,
                assignmentTitle = assignmentTitle,
                assignmentContent = assignmentContent,
            ),
        )

        return withRound(session)
    }

    @Transactional(readOnly = true)
    fun getSessions(
        requester: AuthUser,
        seminarId: Long,
    ): List<SessionWithRound> {
        checkSeminarExists(seminarId)
        requireViewer(requester, seminarId)

        return sessionRepository
            .findAllBySeminarIdOrderByStartsAtAscIdAsc(seminarId)
            .mapIndexed { index, session -> SessionWithRound(session, index + 1) }
    }

    @Transactional(readOnly = true)
    fun getSession(
        requester: AuthUser,
        sessionId: Long,
    ): SessionWithRound {
        val session = findSession(sessionId)
        requireViewer(requester, session.seminarId)

        return withRound(session)
    }

    @Transactional
    fun updateSession(
        requester: AuthUser,
        sessionId: Long,
        request: SessionUpdateRequest,
    ): SessionWithRound {
        val session = findSession(sessionId)
        requester.requireStaffOf(session.seminarId)

        requireNotBlankIfPresent(request.title, "title")
        requireNotBlankIfPresent(request.location, "location")
        requireNotBlankIfPresent(request.assignmentTitle, "assignmentTitle")
        if (request.startsAt.isPresent && request.startsAt.get() == null) {
            throw InvalidSessionRequestException("startsAt must not be null.")
        }

        val updated = sessionRepository.save(
            session.copy(
                title = request.title.orElse(session.title)!!,
                startsAt = request.startsAt.orElse(null)?.toUtc() ?: session.startsAt,
                location = request.location.orElse(session.location)!!,
                assignmentTitle = request.assignmentTitle.orElse(session.assignmentTitle)!!,
                lectureContent = request.lectureContent.orElse(session.lectureContent),
                assignmentContent = request.assignmentContent.orElse(session.assignmentContent),
            ),
        )

        return withRound(updated)
    }

    private fun requireViewer(
        requester: AuthUser,
        seminarId: Long,
    ) = requester.requireMemberOf(seminarId) {
        enrollmentRepository.existsBySeminarIdAndRookieId(seminarId, requester.id)
    }

    private fun requireNotBlankIfPresent(
        value: JsonNullable<String?>,
        name: String,
    ) {
        if (value.isPresent && value.get().isNullOrBlank()) {
            throw InvalidSessionRequestException("$name must not be blank.")
        }
    }

    private fun findSession(sessionId: Long): Session =
        sessionRepository.findByIdOrNull(sessionId)
            ?: throw SessionNotFoundException(sessionId)

    private fun withRound(session: Session): SessionWithRound =
        SessionWithRound(
            session = session,
            round = sessionRepository.countRound(
                seminarId = session.seminarId,
                startsAt = session.startsAt,
                id = session.id!!,
            ),
        )

    private fun checkSeminarExists(seminarId: Long) {
        if (!seminarRepository.existsById(seminarId)) {
            throw SeminarNotFoundException(seminarId)
        }
    }

    private fun OffsetDateTime.toUtc() = withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
}
