package com.wafflestudio.spring2026.team

import com.wafflestudio.spring2026.auth.repository.AuthUserRepository
import com.wafflestudio.spring2026.session.model.AssignmentStatus
import com.wafflestudio.spring2026.session.model.Attendance
import com.wafflestudio.spring2026.session.model.RecordChange
import com.wafflestudio.spring2026.session.model.SessionRecord
import com.wafflestudio.spring2026.session.repository.SessionRecordRepository
import com.wafflestudio.spring2026.session.service.SessionRecordService
import com.wafflestudio.spring2026.support.Week4ApiIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.simple.JdbcClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@Import(SessionRecordsRollbackTest.FailureInjection::class)
class SessionRecordsRollbackTest : Week4ApiIntegrationTest() {
    @Autowired
    private lateinit var sessionRecordService: SessionRecordService

    @Autowired
    private lateinit var failingRepository: FailingSessionRecordRepository

    @Autowired
    private lateinit var authUserRepository: AuthUserRepository

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @AfterEach
    fun disarm() {
        failingRepository.disarm()
    }

    @Test
    fun `첫 DB 쓰기 직후 실패하면 그 쓰기도 되돌린다`() {
        val fixture = prepare()

        failingRepository.armAfterUpserts(1)
        val failure = assertFailsWith<InjectedFailure> {
            sessionRecordService.updateRecords(admin(), fixture.sessionId, fixture.changes)
        }

        assertEquals(1, failure.upserts)
        assertEquals(
            SessionRecord(failure.snapshot.getValue(fixture.first).id, fixture.sessionId, fixture.first, Attendance.ABSENT, null),
            failure.snapshot.getValue(fixture.first),
        )
        assertEquals(fixture.before, committedRecords(fixture.sessionId))
    }

    @Test
    fun `생성 수정 삭제를 모두 실행한 뒤 실패해도 작업 전 상태로 되돌린다`() {
        val fixture = prepare()

        failingRepository.armAfterUpserts(3)
        val failure = assertFailsWith<InjectedFailure> {
            sessionRecordService.updateRecords(admin(), fixture.sessionId, fixture.changes)
        }

        assertEquals(3, failure.upserts)
        assertEquals(Attendance.ABSENT, failure.snapshot.getValue(fixture.first).attendanceStatus)
        assertEquals(null, failure.snapshot.getValue(fixture.first).assignmentStatus)
        assertEquals(false, failure.snapshot.containsKey(fixture.second))
        assertEquals(Attendance.PRESENT, failure.snapshot.getValue(fixture.third).attendanceStatus)
        assertNotEquals(fixture.before, failure.snapshot.mapValues { it.value.attendanceStatus to it.value.assignmentStatus })

        assertEquals(fixture.before, committedRecords(fixture.sessionId))
    }

    @Test
    fun `실패를 주입하지 않으면 같은 요청이 모두 반영된다`() {
        val fixture = prepare()

        sessionRecordService.updateRecords(admin(), fixture.sessionId, fixture.changes)

        assertEquals(
            mapOf(
                fixture.first to (Attendance.ABSENT to null),
                fixture.third to (Attendance.PRESENT to null),
            ),
            committedRecords(fixture.sessionId),
        )
    }

    private data class Fixture(
        val sessionId: Long,
        val first: Long,
        val second: Long,
        val third: Long,
        val changes: List<RecordChange>,
        val before: Map<Long, Pair<Attendance?, AssignmentStatus?>>,
    )

    private fun prepare(): Fixture {
        val seminarId = createSeminar()
        val sessionId = createSession(seminarId)
        val first = enrolledRookie(seminarId).enrollmentId
        val second = enrolledRookie(seminarId).enrollmentId
        val third = enrolledRookie(seminarId).enrollmentId
        val admin = adminToken()
        updateRecords(
            sessionId,
            admin,
            recordPatch(first, "attendance" to "PRESENT", "assignmentStatus" to "PASSED"),
            recordPatch(second, "attendance" to "ABSENT"),
        ).andExpect { status { isNoContent() } }

        val before = committedRecords(sessionId)
        assertEquals(
            mapOf(
                first to (Attendance.PRESENT to AssignmentStatus.PASSED),
                second to (Attendance.ABSENT to null),
            ),
            before,
        )

        val changes = listOf(
            RecordChange(first, attendance = JsonNullable.of(Attendance.ABSENT), assignmentStatus = JsonNullable.of(null)),
            RecordChange(second, attendance = JsonNullable.of(null)),
            RecordChange(third, attendance = JsonNullable.of(Attendance.PRESENT)),
        )

        return Fixture(sessionId, first, second, third, changes, before)
    }

    private fun admin() = authUserRepository.findByEmail(ADMIN_EMAIL)!!

    private fun committedRecords(sessionId: Long): Map<Long, Pair<Attendance?, AssignmentStatus?>> =
        jdbcClient
            .sql("SELECT enrollment_id, attendance_status, assignment_status FROM session_record WHERE session_id = :sessionId")
            .param("sessionId", sessionId)
            .query { rs, _ ->
                rs.getLong("enrollment_id") to (
                    rs.getString("attendance_status")?.let(Attendance::valueOf) to
                        rs.getString("assignment_status")?.let(AssignmentStatus::valueOf)
                )
            }.list()
            .toMap()

    class InjectedFailure(
        val upserts: Int,
        val snapshot: Map<Long, SessionRecord>,
    ) : RuntimeException("Injected failure after $upserts upserts")

    class FailingSessionRecordRepository(
        private val delegate: SessionRecordRepository,
    ) : SessionRecordRepository by delegate {
        private var failAfter: Int? = null
        private var upserts = 0
        private val touched = mutableListOf<Pair<Long, Long>>()

        fun armAfterUpserts(count: Int) {
            failAfter = count
            upserts = 0
            touched.clear()
        }

        fun disarm() {
            failAfter = null
            upserts = 0
            touched.clear()
        }

        override fun upsert(
            sessionId: Long,
            enrollmentId: Long,
            attendanceSet: Boolean,
            attendance: String?,
            assignmentStatusSet: Boolean,
            assignmentStatus: String?,
        ) {
            delegate.upsert(sessionId, enrollmentId, attendanceSet, attendance, assignmentStatusSet, assignmentStatus)
            val limit = failAfter ?: return
            touched += sessionId to enrollmentId
            upserts++
            if (upserts >= limit) {
                val snapshot = touched
                    .mapNotNull { (session, enrollment) -> delegate.findBySessionIdAndEnrollmentId(session, enrollment) }
                    .associateBy { it.enrollmentId }
                throw InjectedFailure(upserts, snapshot)
            }
        }

        override fun deleteIfEmpty(
            sessionId: Long,
            enrollmentId: Long,
        ) {
            delegate.deleteIfEmpty(sessionId, enrollmentId)
        }
    }

    @TestConfiguration
    class FailureInjection {
        @Bean
        @Primary
        fun failingSessionRecordRepository(
            @Qualifier("sessionRecordRepository") delegate: SessionRecordRepository,
        ): FailingSessionRecordRepository = FailingSessionRecordRepository(delegate)
    }
}
