package com.wafflestudio.spring2026.team.dashboard

import com.wafflestudio.spring2026.support.Week4ApiIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// 팀 자체 대시보드 회귀 테스트.
// 기록·탈락 상태는 팀 스키마(session_record, enrollment)에 직접 넣어 준비한다.
// 기록 수정·수강 정보 수정 API 구현과 관계없이 대시보드 조회만 검증하기 위해서다.
class TeamDashboardRegressionTest : Week4ApiIntegrationTest() {
    @Autowired
    private lateinit var jdbcClient: JdbcClient

    private fun insertRecord(
        sessionId: Long,
        enrollmentId: Long,
        attendance: String?,
        assignmentStatus: String?,
    ) {
        jdbcClient
            .sql(
                """
                INSERT INTO session_record (session_id, enrollment_id, attendance_status, assignment_status)
                VALUES (:sessionId, :enrollmentId, :attendance, :assignmentStatus)
                """,
            ).param("sessionId", sessionId)
            .param("enrollmentId", enrollmentId)
            .param("attendance", attendance)
            .param("assignmentStatus", assignmentStatus)
            .update()
    }

    private fun markDropped(enrollmentId: Long) {
        jdbcClient
            .sql("UPDATE enrollment SET dropped = TRUE WHERE id = :id")
            .param("id", enrollmentId)
            .update()
    }

    private fun recordCount(enrollmentId: Long): Long =
        jdbcClient
            .sql("SELECT COUNT(*) FROM session_record WHERE enrollment_id = :id")
            .param("id", enrollmentId)
            .query(Long::class.javaObjectType)
            .single()

    private fun rowIds(body: tools.jackson.databind.JsonNode): List<Long> =
        body.path("content").toList().map { it.path("id").asLong() }

    @Test
    fun `와장·담당 운영진·수강 중인 루키만 조회할 수 있다`() {
        val seminarId = createSeminar()
        val otherSeminarId = createSeminar()
        val me = enrolledRookie(seminarId)
        val classmate = enrolledRookie(seminarId)
        val expectedIds = listOf(me.enrollmentId, classmate.enrollmentId).sorted()

        assertEquals(expectedIds, rowIds(dashboard(seminarId, adminToken())))
        assertEquals(expectedIds, rowIds(dashboard(seminarId, approvedStaff(seminarId).token)))
        // 수강 중인 루키는 자신뿐 아니라 같은 세미나의 다른 수강생도 본다.
        assertEquals(expectedIds, rowIds(dashboard(seminarId, me.actor.token)))

        val path = "/seminars/$seminarId/dashboard"
        getAs(approvedStaff(otherSeminarId).token, path).andExpect { status { isForbidden() } }
        getAs(approvedRookie().token, path).andExpect { status { isForbidden() } }
        getAs(enrolledRookie(otherSeminarId).actor.token, path).andExpect { status { isForbidden() } }
        getAs(pendingStaff(seminarId).token, path).andExpect { status { isForbidden() } }
        getAs(pendingRookie().token, path).andExpect { status { isForbidden() } }
        getAs(rejectedRookie().token, path).andExpect { status { isForbidden() } }
        getAs(null, path).andExpect { status { isUnauthorized() } }
        getAs(null, "/seminars/$MISSING_ID/dashboard").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `탈락 표시된 수강생도 목록과 전체 수에 포함되고 계속 조회할 수 있다`() {
        val seminarId = createSeminar()
        val dropped = enrolledRookie(seminarId)
        val active = enrolledRookie(seminarId)
        markDropped(dropped.enrollmentId)

        val body = dashboard(seminarId, dropped.actor.token)
        assertEquals(2, body.path("totalElements").asInt())
        assertEquals(true, dashboardRow(body, dropped.enrollmentId).path("dropped").asBoolean())
        assertEquals(false, dashboardRow(body, active.enrollmentId).path("dropped").asBoolean())
    }

    @Test
    fun `저장된 기록을 회차 ID에 맞춰 보여 주고 한쪽만 기록된 칸은 null이다`() {
        val seminarId = createSeminar()
        val start = now().plusDays(1)
        val first = createSession(seminarId, startsAt = start)
        val second = createSession(seminarId, startsAt = start.plusDays(1))
        val third = createSession(seminarId, startsAt = start.plusDays(2))
        val student = enrolledRookie(seminarId)
        val other = enrolledRookie(seminarId)

        insertRecord(first, student.enrollmentId, "PRESENT", "PASSED")
        insertRecord(second, student.enrollmentId, "ABSENT", null)
        insertRecord(third, student.enrollmentId, null, "SUBMITTED_BUT_FAILED")
        insertRecord(first, other.enrollmentId, null, "NOT_SUBMITTED")

        val body = dashboard(seminarId, adminToken())
        assertRecord(body, student.enrollmentId, first, "PRESENT", "PASSED")
        assertRecord(body, student.enrollmentId, second, "ABSENT", null)
        assertRecord(body, student.enrollmentId, third, null, "SUBMITTED_BUT_FAILED")
        assertRecord(body, other.enrollmentId, first, null, "NOT_SUBMITTED")
        // 다른 수강생의 기록이 섞이지 않는다.
        assertRecord(body, other.enrollmentId, second, null, null)
        assertRecord(body, other.enrollmentId, third, null, null)
    }

    @Test
    fun `다른 세미나의 회차와 수강생은 대시보드에 나오지 않는다`() {
        val seminarId = createSeminar()
        val otherSeminarId = createSeminar()
        val sessionId = createSession(seminarId)
        val otherSessionId = createSession(otherSeminarId)
        val student = enrolledRookie(seminarId)
        val outsider = enrolledRookie(otherSeminarId)
        insertRecord(otherSessionId, outsider.enrollmentId, "PRESENT", "PASSED")

        val body = dashboard(seminarId, adminToken())
        assertEquals(listOf(sessionId), body.path("sessions").toList().map { it.path("id").asLong() })
        assertEquals(listOf(student.enrollmentId), rowIds(body))
        assertEquals(1, body.path("totalElements").asInt())
    }

    @Test
    fun `취소하면 행과 기록이 사라지고 조회 권한을 잃으며 재신청하면 초기 상태로 시작한다`() {
        val seminarId = createSeminar(totalGraceDays = 5)
        val sessionId = createSession(seminarId)
        val student = enrolledRookie(seminarId)
        val classmate = enrolledRookie(seminarId)
        markDropped(student.enrollmentId)
        jdbcClient
            .sql("UPDATE enrollment SET grace_days_remaining = 1 WHERE id = :id")
            .param("id", student.enrollmentId)
            .update()
        insertRecord(sessionId, student.enrollmentId, "PRESENT", "PASSED")
        insertRecord(sessionId, classmate.enrollmentId, "ABSENT", "NOT_SUBMITTED")

        cancelEnrollment(seminarId, student.actor.token).andExpect { status { isNoContent() } }

        getAs(student.actor.token, "/seminars/$seminarId/dashboard").andExpect { status { isForbidden() } }
        val afterCancel = dashboard(seminarId, adminToken())
        assertEquals(listOf(classmate.enrollmentId), rowIds(afterCancel))
        assertEquals(0, recordCount(student.enrollmentId))
        // 다른 수강생의 기록과 회차는 유지된다.
        assertRecord(afterCancel, classmate.enrollmentId, sessionId, "ABSENT", "NOT_SUBMITTED")
        assertEquals(listOf(sessionId), afterCancel.path("sessions").toList().map { it.path("id").asLong() })

        val newEnrollmentId = enrollSuccessfully(seminarId, student.actor)
        assertNotEquals(student.enrollmentId, newEnrollmentId)
        val afterReenroll = dashboard(seminarId, student.actor.token)
        val row = dashboardRow(afterReenroll, newEnrollmentId)
        assertEquals(5, row.path("graceDaysRemaining").asInt())
        assertEquals(false, row.path("dropped").asBoolean())
        assertRecord(afterReenroll, newEnrollmentId, sessionId, null, null)
    }

    @Test
    fun `페이지 크기 경계값 1과 100을 허용한다`() {
        val seminarId = createSeminar()
        val students = List(3) { enrolledRookie(seminarId) }
        val admin = adminToken()

        val first = dashboard(seminarId, admin, "?size=1")
        assertEquals(listOf(students.minOf { it.enrollmentId }), rowIds(first))
        assertEquals(3, first.path("totalPages").asInt())

        val all = dashboard(seminarId, admin, "?page=0&size=100")
        assertEquals(students.map { it.enrollmentId }.sorted(), rowIds(all))
        assertTrue(all.path("totalPages").asInt() == 1)
    }
}
