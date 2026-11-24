package com.wafflestudio.spring2026.session.model

import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

enum class Attendance {
    PRESENT,
    ABSENT,
}

enum class AssignmentStatus {
    PASSED,
    SUBMITTED_BUT_FAILED,
    NOT_SUBMITTED,
}

@Table("session_record")
data class SessionRecord(
    @Id
    val id: Long? = null,
    val sessionId: Long,
    val enrollmentId: Long,
    val attendanceStatus: Attendance?,
    val assignmentStatus: AssignmentStatus?,
)

data class RecordChange(
    val enrollmentId: Long,
    val attendance: JsonNullable<Attendance?> = JsonNullable.undefined(),
    val assignmentStatus: JsonNullable<AssignmentStatus?> = JsonNullable.undefined(),
)
