package com.wafflestudio.spring2026.enrollment.repository

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import org.springframework.data.repository.CrudRepository

interface EnrollmentRepository : CrudRepository<Enrollment, Long> {
    fun existsBySeminarIdAndRookieId(
        seminarId: Long,
        rookieId: Long,
    ): Boolean

    fun findByIdAndSeminarId(
        id: Long,
        seminarId: Long,
    ): Enrollment?
}
