package com.wafflestudio.spring2026.team

import com.wafflestudio.spring2026.support.Week4ApiIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class EnrollmentConcurrencyTeamTest : Week4ApiIntegrationTest() {
    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @Test
    fun `100 rookies racing for 30 seats yield exactly 30 enrollments`() {
        val seminarId = createSeminar(capacity = 30)
        val rookies = List(100) { approvedRookie() }

        val statuses = concurrently(rookies) { enrollStatus(seminarId, it.token) }

        assertEquals(mapOf(201 to 30, 409 to 70), statuses.tally())
        assertEquals(30, enrollmentCount(seminarId))
        assertEquals(30, distinctRookieCount(seminarId))
        assertSeminar(seminarId, enrolledCount = 30, status = "CLOSED")
    }

    @Test
    fun `20 concurrent enrollments by one rookie create a single enrollment`() {
        val seminarId = createSeminar(capacity = 10)
        val rookie = approvedRookie()

        val statuses = concurrently(List(20) { rookie }) { enrollStatus(seminarId, it.token) }

        assertEquals(mapOf(201 to 1, 409 to 19), statuses.tally())
        assertEquals(1, enrollmentCountOf(seminarId, rookie.id))
        assertEquals(1, enrollmentCount(seminarId))
        assertSeminar(seminarId, enrolledCount = 1, status = "OPEN")
    }

    @Test
    fun `10 concurrent cancellations by one rookie decrease the headcount once`() {
        val seminarId = createSeminar(capacity = 10)
        val leaving = enrolledRookie(seminarId)
        enrolledRookie(seminarId)

        val statuses = concurrently(List(10) { leaving }) { cancelStatus(seminarId, it.actor.token) }

        assertEquals(mapOf(204 to 1, 404 to 9), statuses.tally())
        assertEquals(0, enrollmentCountOf(seminarId, leaving.actor.id))
        assertEquals(1, enrollmentCount(seminarId))
        assertSeminar(seminarId, enrolledCount = 1, status = "OPEN")
    }

    @Test
    fun `mixed cancellations and enrollments keep the headcount equation and capacity`() {
        val capacity = 5
        val seminarId = createSeminar(capacity = capacity)
        val members = List(capacity) { enrolledRookie(seminarId) }
        val newcomers = List(10) { approvedRookie() }
        assertSeminar(seminarId, enrolledCount = capacity, status = "CLOSED")

        val tasks: List<() -> Pair<String, Int>> =
            members.map { member -> { "cancel" to cancelStatus(seminarId, member.actor.token) } } +
                newcomers.map { rookie -> { "enroll" to enrollStatus(seminarId, rookie.token) } }

        val results = concurrently(tasks.shuffled()) { it() }

        val cancels = results.filter { it.first == "cancel" }.map { it.second }
        val enrolls = results.filter { it.first == "enroll" }.map { it.second }
        assertTrue(cancels.all { it == 204 }, "Unexpected cancel statuses: $cancels")
        assertTrue(enrolls.all { it == 201 || it == 409 }, "Unexpected enroll statuses: $enrolls")

        val expected = capacity + enrolls.count { it == 201 } - cancels.count { it == 204 }
        val actual = enrollmentCount(seminarId)
        assertEquals(expected, actual)
        assertTrue(actual in 0..capacity)
        assertEquals(actual, distinctRookieCount(seminarId))
        assertSeminar(seminarId, enrolledCount = actual, status = if (actual == capacity) "CLOSED" else "OPEN")

        repeat(capacity - actual) {
            assertEquals(201, enrollStatus(seminarId, approvedRookie().token))
        }
        assertEquals(capacity, enrollmentCount(seminarId))
        assertEquals(409, enrollStatus(seminarId, approvedRookie().token))
        assertSeminar(seminarId, enrolledCount = capacity, status = "CLOSED")
    }

    private fun enrollStatus(seminarId: Long, token: String): Int =
        enroll(seminarId, token).andReturn().response.status

    private fun cancelStatus(seminarId: Long, token: String): Int =
        cancelEnrollment(seminarId, token).andReturn().response.status

    private fun assertSeminar(seminarId: Long, enrolledCount: Int, status: String) {
        val seminar = responseJson(getAs(adminToken(), "/seminars/$seminarId").andExpect { status { isOk() } })
        assertEquals(enrolledCount, seminar.path("enrolledCount").asInt())
        assertEquals(status, seminar.path("status").asString())
    }

    private fun enrollmentCount(seminarId: Long): Int =
        jdbcClient
            .sql("SELECT COUNT(*) FROM enrollment WHERE seminar_id = :seminarId")
            .param("seminarId", seminarId)
            .query(Int::class.java)
            .single()

    private fun distinctRookieCount(seminarId: Long): Int =
        jdbcClient
            .sql("SELECT COUNT(DISTINCT rookie_id) FROM enrollment WHERE seminar_id = :seminarId")
            .param("seminarId", seminarId)
            .query(Int::class.java)
            .single()

    private fun enrollmentCountOf(seminarId: Long, rookieId: Long): Int =
        jdbcClient
            .sql("SELECT COUNT(*) FROM enrollment WHERE seminar_id = :seminarId AND rookie_id = :rookieId")
            .param("seminarId", seminarId)
            .param("rookieId", rookieId)
            .query(Int::class.java)
            .single()

    private fun List<Int>.tally(): Map<Int, Int> = groupingBy { it }.eachCount()

    private fun <T, R> concurrently(inputs: List<T>, action: (T) -> R): List<R> {
        val workers = minOf(THREADS, inputs.size)
        val ready = CountDownLatch(workers)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(workers)
        try {
            val futures = inputs.map { input ->
                executor.submit(
                    Callable {
                        ready.countDown()
                        start.await()
                        action(input)
                    },
                )
            }
            assertTrue(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Workers did not get ready in time.")
            start.countDown()

            return futures.mapIndexed { index, future ->
                try {
                    future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                } catch (exception: ExecutionException) {
                    fail("Task #$index failed unexpectedly.", exception.cause)
                }
            }
        } finally {
            start.countDown()
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }

    private companion object {
        const val THREADS = 32
        const val TIMEOUT_SECONDS = 60L
    }
}
