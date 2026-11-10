package com.wafflestudio.spring2026.seminar.service

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.PageResponse
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.seminar.InvalidSeminarRequestException
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.model.Seminar
import com.wafflestudio.spring2026.seminar.model.SeminarDetail
import com.wafflestudio.spring2026.seminar.model.SeminarStatus
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class SeminarService(
    private val seminarRepository: SeminarRepository,
) {
    @Transactional
    fun createSeminar(
        requester: AuthUser,
        title: String,
        description: String?,
        capacity: Int,
        applyStartAt: OffsetDateTime,
        applyEndAt: OffsetDateTime,
        totalGraceDays: Int,
    ): Seminar {
        requester.requireAdmin()
        if (!applyEndAt.isAfter(applyStartAt)) {
            throw InvalidSeminarRequestException("Application end time must be after the start time.")
        }

        return seminarRepository.save(
            Seminar(
                title = title,
                description = description,
                capacity = capacity,
                applyStartAt = applyStartAt.toUtc(),
                applyEndAt = applyEndAt.toUtc(),
                totalGraceDays = totalGraceDays,
                createdAt = nowUtc(),
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getSeminar(
        requester: AuthUser,
        seminarId: Long,
    ): SeminarDetail {
        requester.requireApproved()
        return detailOf(findSeminar(seminarId))
    }

    @Transactional(readOnly = true)
    fun searchSeminars(
        keyword: String?,
        status: SeminarStatus?,
        pageQuery: PageQuery,
    ): PageResponse<SeminarDetail> {
        val normalizedKeyword = keyword?.takeIf { it.isNotBlank() }
        val now = nowUtc()

        val seminars = seminarRepository.search(normalizedKeyword, status?.name, now, pageQuery.size, pageQuery.offset)
        val total = seminarRepository.countSearch(normalizedKeyword, status?.name, now)

        return pageQuery.toResponse(seminars.map(::detailOf), total)
    }

    @Transactional
    fun updateSeminar(
        requester: AuthUser,
        seminarId: Long,
        title: JsonNullable<String?>,
        description: JsonNullable<String?>,
    ): SeminarDetail {
        requester.requireStaffOf(seminarId)
        if (title.isPresent && title.get().isNullOrBlank()) {
            throw InvalidSeminarRequestException("Seminar title must not be blank.")
        }

        val seminar = findSeminar(seminarId)
        val updated = seminarRepository.save(
            seminar.copy(
                title = if (title.isPresent) title.get()!! else seminar.title,
                description = if (description.isPresent) description.get() else seminar.description,
            ),
        )

        return detailOf(updated)
    }

    private fun findSeminar(seminarId: Long): Seminar =
        seminarRepository.findByIdOrNull(seminarId) ?: throw SeminarNotFoundException(seminarId)

    private fun detailOf(seminar: Seminar): SeminarDetail {
        val enrolledCount = seminarRepository.countEnrollments(seminar.id!!)

        return SeminarDetail(
            seminar = seminar,
            enrolledCount = enrolledCount,
            sessionCount = seminarRepository.countSessions(seminar.id),
            status = seminar.statusAt(nowUtc(), enrolledCount),
        )
    }

    private fun OffsetDateTime.toUtc(): LocalDateTime = withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()

    private fun nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
