package com.wafflestudio.spring2026.team

import com.wafflestudio.spring2026.support.Week4ApiIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import kotlin.test.Test
import kotlin.test.assertEquals

class EnrollmentUpdateTeamTest : Week4ApiIntegrationTest() {
    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @Test
    fun `staff of the seminar updates only the fields sent`() {
        val seminarId = createSeminar(totalGraceDays = 3)
        val staff = approvedStaff(seminarId)
        val enrolled = enrolledRookie(seminarId)

        updateEnrollment(seminarId, enrolled.enrollmentId, staff.token, mapOf("dropped" to true))
            .andExpect {
                status { isOk() }
                jsonPath("$.graceDaysRemaining") { value(3) }
                jsonPath("$.dropped") { value(true) }
            }
        updateEnrollment(seminarId, enrolled.enrollmentId, staff.token, mapOf("graceDaysRemaining" to 1))
            .andExpect {
                status { isOk() }
                jsonPath("$.graceDaysRemaining") { value(1) }
                jsonPath("$.dropped") { value(true) }
            }

        val mine = myEnrollments(enrolled.actor).single()
        assertEquals(1, mine.path("graceDaysRemaining").asInt())
        assertEquals(true, mine.path("dropped").asBoolean())
    }

    @Test
    fun `other staff and rookies cannot update an enrollment`() {
        val seminarId = createSeminar()
        val otherSeminarStaff = approvedStaff(createSeminar())
        val enrolled = enrolledRookie(seminarId)
        val body = mapOf("dropped" to true)

        updateEnrollment(seminarId, enrolled.enrollmentId, otherSeminarStaff.token, body)
            .andExpect { status { isForbidden() } }
        updateEnrollment(seminarId, enrolled.enrollmentId, enrolled.actor.token, body)
            .andExpect { status { isForbidden() } }
        assertEquals(false, myEnrollments(enrolled.actor).single().path("dropped").asBoolean())
    }

    @Test
    fun `enrollment of another seminar is not found through this seminar`() {
        val seminarId = createSeminar()
        val other = enrolledRookie(createSeminar())

        updateEnrollment(seminarId, other.enrollmentId, adminToken(), mapOf("dropped" to true))
            .andExpect { status { isNotFound() } }
        updateEnrollment(MISSING_ID, other.enrollmentId, adminToken(), mapOf("dropped" to true))
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `cancelling removes records and re-enrolling starts fresh`() {
        val seminarId = createSeminar(totalGraceDays = 4)
        val sessionId = createSession(seminarId)
        val enrolled = enrolledRookie(seminarId)
        val classmate = enrolledRookie(seminarId)
        val admin = adminToken()
        updateEnrollment(seminarId, enrolled.enrollmentId, admin, mapOf("graceDaysRemaining" to 0, "dropped" to true))
            .andExpect { status { isOk() } }
        updateRecords(
            sessionId,
            admin,
            recordPatch(enrolled.enrollmentId, "attendance" to "PRESENT"),
            recordPatch(classmate.enrollmentId, "attendance" to "ABSENT"),
        ).andExpect { status { isNoContent() } }

        cancelEnrollment(seminarId, enrolled.actor.token).andExpect { status { isNoContent() } }

        assertEquals(0, recordCount(enrolled.enrollmentId))
        assertEquals(1, recordCount(classmate.enrollmentId))

        val reEnrolledId = enrollSuccessfully(seminarId, enrolled.actor)
        val row = dashboard(seminarId, admin).path("content").single { it.path("id").asLong() == reEnrolledId }
        assertEquals(4, row.path("graceDaysRemaining").asInt())
        assertEquals(false, row.path("dropped").asBoolean())
        assertEquals(true, row.path("records").single().path("attendance").isNull)
    }

    private fun recordCount(enrollmentId: Long): Int =
        jdbcClient
            .sql("SELECT COUNT(*) FROM session_record WHERE enrollment_id = :enrollmentId")
            .param("enrollmentId", enrollmentId)
            .query(Int::class.java)
            .single()
}
