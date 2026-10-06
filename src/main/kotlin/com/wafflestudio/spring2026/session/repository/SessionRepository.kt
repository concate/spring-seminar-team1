package com.wafflestudio.spring2026.session.repository

import com.wafflestudio.spring2026.session.model.Session
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface SessionRepository : CrudRepository<Session, Long> {
    fun findAllBySeminarIdOrderByStartsAtAscIdAsc(seminarId: Long): List<Session>

    // 세미나 안에서 (starts_at, id) 오름차순으로 정렬했을 때 이 회차가 몇 번째인지 셉니다.
    @Query(
        """
        SELECT COUNT(*) FROM session
        WHERE seminar_id = :seminarId
          AND (starts_at < :startsAt OR (starts_at = :startsAt AND id <= :id))
        """,
    )
    fun countRound(
        @Param("seminarId") seminarId: Long,
        @Param("startsAt") startsAt: LocalDateTime,
        @Param("id") id: Long,
    ): Int

    // TODO: 세미나 기능이 머지되면 SeminarRepository.existsById 로 교체합니다.
    @Query("SELECT EXISTS(SELECT 1 FROM seminar WHERE id = :seminarId)")
    fun existsSeminarById(
        @Param("seminarId") seminarId: Long,
    ): Boolean
}
