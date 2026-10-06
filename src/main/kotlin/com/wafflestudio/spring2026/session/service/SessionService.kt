package com.wafflestudio.spring2026.session.service

import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.SessionNotFoundException
import com.wafflestudio.spring2026.session.model.Session
import com.wafflestudio.spring2026.session.model.SessionWithRound
import com.wafflestudio.spring2026.session.repository.SessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val seminarRepository: SeminarRepository,
) {
    @Transactional
    fun createSession(
        seminarId: Long,
        title: String,
        startsAt: OffsetDateTime,
        location: String,
        assignmentTitle: String,
        lectureContent: String?,
        assignmentContent: String?,
    ): SessionWithRound {
        checkSeminarExists(seminarId)

        val session = sessionRepository.save(
            Session(
                seminarId = seminarId,
                title = title,
                startsAt = startsAt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                location = location,
                lectureContent = lectureContent,
                assignmentTitle = assignmentTitle,
                assignmentContent = assignmentContent,
            ),
        )

        return withRound(session)
    }

    @Transactional(readOnly = true)
    fun getSessions(seminarId: Long): List<SessionWithRound> {
        checkSeminarExists(seminarId)

        return sessionRepository
            .findAllBySeminarIdOrderByStartsAtAscIdAsc(seminarId)
            .mapIndexed { index, session -> SessionWithRound(session, index + 1) }
    }

    @Transactional(readOnly = true)
    fun getSession(sessionId: Long): SessionWithRound {
        val session = sessionRepository.findByIdOrNull(sessionId)
            ?: throw SessionNotFoundException(sessionId)

        return withRound(session)
    }

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
}
