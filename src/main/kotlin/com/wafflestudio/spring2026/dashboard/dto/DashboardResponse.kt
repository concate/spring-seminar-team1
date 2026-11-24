package com.wafflestudio.spring2026.dashboard.dto

import java.time.OffsetDateTime

data class DashboardResponse(
    val seminarId: Long,
    val sessions: List<DashboardSessionResponse>,
    val content: List<DashboardRowResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class DashboardSessionResponse(
    val id: Long,
    val round: Int,
    val title: String,
    val startsAt: OffsetDateTime,
    val assignmentTitle: String,
)

// id 는 사용자 ID 가 아니라 수강 신청(enrollment) ID 다.
data class DashboardRowResponse(
    val id: Long,
    val user: DashboardUserResponse,
    val graceDaysRemaining: Int,
    val dropped: Boolean,
    val createdAt: OffsetDateTime,
    val records: List<DashboardRecordResponse>,
)

data class DashboardUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val githubUsername: String,
)

// 미기록은 null 로 응답한다. 프로퍼티를 생략하지 않는다.
data class DashboardRecordResponse(
    val sessionId: Long,
    val attendance: String?,
    val assignmentStatus: String?,
)
