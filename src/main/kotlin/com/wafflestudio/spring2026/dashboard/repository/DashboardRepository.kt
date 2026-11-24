package com.wafflestudio.spring2026.dashboard.repository

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

// 대시보드 화면 전용 읽기 쿼리. 수강 신청·사용자·출석 기록 테이블을 조인해 읽기만 하고 쓰지 않는다.
// 기록 수정(PATCH /sessions/{sessionId}/records)은 별도 패키지에서 담당한다.
@Repository
class DashboardRepository(
    private val jdbcClient: JdbcClient,
) {
    data class EnrollmentRow(
        val enrollmentId: Long,
        val userId: Long,
        val email: String,
        val name: String,
        val githubUsername: String,
        val graceDaysRemaining: Int,
        val dropped: Boolean,
        val createdAt: LocalDateTime,
    )

    data class RecordRow(
        val enrollmentId: Long,
        val sessionId: Long,
        val attendance: String?,
        val assignmentStatus: String?,
    )

    fun findEnrollmentPage(
        seminarId: Long,
        limit: Int,
        offset: Long,
    ): List<EnrollmentRow> =
        jdbcClient
            .sql(
                """
                SELECT e.id, e.rookie_id, u.email, u.name, u.github_username,
                       e.grace_days_remaining, e.dropped, e.created_at
                FROM enrollment e
                JOIN `user` u ON u.id = e.rookie_id
                WHERE e.seminar_id = :seminarId
                ORDER BY e.id
                LIMIT :limit OFFSET :offset
                """,
            ).param("seminarId", seminarId)
            .param("limit", limit)
            .param("offset", offset)
            .query { rs, _ ->
                EnrollmentRow(
                    enrollmentId = rs.getLong("id"),
                    userId = rs.getLong("rookie_id"),
                    email = rs.getString("email"),
                    name = rs.getString("name"),
                    githubUsername = rs.getString("github_username"),
                    graceDaysRemaining = rs.getInt("grace_days_remaining"),
                    dropped = rs.getBoolean("dropped"),
                    createdAt = rs.getObject("created_at", LocalDateTime::class.java),
                )
            }.list()

    fun countEnrollments(seminarId: Long): Long =
        jdbcClient
            .sql("SELECT COUNT(*) FROM enrollment WHERE seminar_id = :seminarId")
            .param("seminarId", seminarId)
            .query(Long::class.javaObjectType)
            .single()

    fun findRecords(enrollmentIds: Collection<Long>): List<RecordRow> {
        if (enrollmentIds.isEmpty()) return emptyList()

        return jdbcClient
            .sql(
                """
                SELECT enrollment_id, session_id, attendance_status, assignment_status
                FROM session_record
                WHERE enrollment_id IN (:enrollmentIds)
                """,
            ).param("enrollmentIds", enrollmentIds)
            .query { rs, _ ->
                RecordRow(
                    enrollmentId = rs.getLong("enrollment_id"),
                    sessionId = rs.getLong("session_id"),
                    attendance = rs.getString("attendance_status"),
                    assignmentStatus = rs.getString("assignment_status"),
                )
            }.list()
    }
}
