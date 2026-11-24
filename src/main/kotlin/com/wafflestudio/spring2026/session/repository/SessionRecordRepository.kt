package com.wafflestudio.spring2026.session.repository

import com.wafflestudio.spring2026.session.model.SessionRecord
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SessionRecordRepository : CrudRepository<SessionRecord, Long> {
    fun findBySessionIdAndEnrollmentId(
        sessionId: Long,
        enrollmentId: Long,
    ): SessionRecord?

    @Modifying
    @Query(
        """
        INSERT INTO session_record (session_id, enrollment_id, attendance_status, assignment_status)
        VALUES (:sessionId, :enrollmentId, :attendance, :assignmentStatus)
        ON DUPLICATE KEY UPDATE
            attendance_status = IF(:attendanceSet, :attendance, attendance_status),
            assignment_status = IF(:assignmentStatusSet, :assignmentStatus, assignment_status)
        """,
    )
    fun upsert(
        @Param("sessionId") sessionId: Long,
        @Param("enrollmentId") enrollmentId: Long,
        @Param("attendanceSet") attendanceSet: Boolean,
        @Param("attendance") attendance: String?,
        @Param("assignmentStatusSet") assignmentStatusSet: Boolean,
        @Param("assignmentStatus") assignmentStatus: String?,
    )

    @Modifying
    @Query(
        """
        DELETE FROM session_record
        WHERE session_id = :sessionId
          AND enrollment_id = :enrollmentId
          AND attendance_status IS NULL
          AND assignment_status IS NULL
        """,
    )
    fun deleteIfEmpty(
        @Param("sessionId") sessionId: Long,
        @Param("enrollmentId") enrollmentId: Long,
    )
}
