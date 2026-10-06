package com.wafflestudio.spring2026.seminar.service

import com.wafflestudio.spring2026.seminar.InvalidSeminarRequestException
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.model.Seminar
import com.wafflestudio.spring2026.seminar.model.SeminarDetail
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SeminarService(
    private val seminarRepository: SeminarRepository,
) {
    @Transactional
    fun createSeminar(
        title: String,
        description: String?,
        capacity: Int,
        applyStartAt: OffsetDateTime,
        applyEndAt: OffsetDateTime,
        totalGraceDays: Int,
    ): Seminar {
        if (!applyEndAt.isAfter(applyStartAt)) {
            throw InvalidSeminarRequestException("신청 종료 일시는 신청 시작 일시보다 뒤여야 합니다.")
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
    fun getSeminar(seminarId: Long): SeminarDetail = detailOf(findSeminar(seminarId))

    @Transactional
    fun updateSeminar(
        seminarId: Long,
        title: JsonNullable<String?>,
        description: JsonNullable<String?>,
    ): SeminarDetail {
        if (title.isPresent && title.get().isNullOrBlank()) {
            throw InvalidSeminarRequestException("세미나 제목은 비어 있을 수 없습니다.")
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
        seminarRepository.findByIdOrNull(seminarId)
            ?: throw SeminarNotFoundException(seminarId)

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
