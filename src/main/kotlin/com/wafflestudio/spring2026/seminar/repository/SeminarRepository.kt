package com.wafflestudio.spring2026.seminar.repository

import com.wafflestudio.spring2026.seminar.model.Seminar
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

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
}
