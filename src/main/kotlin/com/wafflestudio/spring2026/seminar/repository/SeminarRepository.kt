package com.wafflestudio.spring2026.seminar.repository

import com.wafflestudio.spring2026.seminar.model.Seminar
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

// status 는 Seminar.statusAt 과 같은 규칙을 SQL 로 옮긴 것이다. 규칙을 바꾸면 둘 다 바꿀 것.
private const val SEARCH_CONDITION = """
    WHERE (:keyword IS NULL OR LOCATE(:keyword, s.title) > 0)
      AND (:status IS NULL OR :status = CASE
            WHEN :now >= s.apply_end_at
              OR (SELECT COUNT(*) FROM enrollment e WHERE e.seminar_id = s.id) >= s.capacity THEN 'CLOSED'
            WHEN :now < s.apply_start_at THEN 'BEFORE'
            ELSE 'OPEN'
          END)
"""

interface SeminarRepository : CrudRepository<Seminar, Long> {
    @Query("SELECT * FROM seminar WHERE id = :id FOR UPDATE")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): Seminar?

    @Query("SELECT COUNT(*) FROM enrollment WHERE seminar_id = :seminarId")
    fun countEnrollments(
        @Param("seminarId") seminarId: Long,
    ): Int

    @Query("SELECT COUNT(*) FROM session WHERE seminar_id = :seminarId")
    fun countSessions(
        @Param("seminarId") seminarId: Long,
    ): Int

    @Query("SELECT s.* FROM seminar s $SEARCH_CONDITION ORDER BY s.id LIMIT :limit OFFSET :offset")
    fun search(
        @Param("keyword") keyword: String?,
        @Param("status") status: String?,
        @Param("now") now: LocalDateTime,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<Seminar>

    @Query("SELECT COUNT(*) FROM seminar s $SEARCH_CONDITION")
    fun countSearch(
        @Param("keyword") keyword: String?,
        @Param("status") status: String?,
        @Param("now") now: LocalDateTime,
    ): Long
}
