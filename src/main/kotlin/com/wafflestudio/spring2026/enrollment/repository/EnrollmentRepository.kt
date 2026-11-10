package com.wafflestudio.spring2026.enrollment.repository

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EnrollmentRepository : CrudRepository<Enrollment, Long> {
    fun existsBySeminarIdAndRookieId(
        seminarId: Long,
        rookieId: Long,
    ): Boolean

    fun findBySeminarIdAndRookieId(
        seminarId: Long,
        rookieId: Long,
    ): Enrollment?

    @Query(
        """
        SELECT * FROM enrollment
        WHERE rookie_id = :rookieId
        ORDER BY created_at DESC, id DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun findPageByRookieId(
        @Param("rookieId") rookieId: Long,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<Enrollment>

    fun countByRookieId(rookieId: Long): Long
}
