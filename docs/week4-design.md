# Week 4 Design: Concurrency Control and Transaction Boundaries

## 1. The problem

Enrollment used to be a read-check-write sequence inside one transaction:

1. Read the seminar and count its enrollments.
2. Check that the apply period is open, the seminar is not full, and the rookie has not enrolled yet.
3. Insert the enrollment row.

Under concurrent requests, this sequence lets several requests pass the check before any of them writes.

**Over-capacity.** A seminar with capacity 30 already has 29 enrollments.

| Time | Request A | Request B |
| --- | --- | --- |
| t1 | `COUNT(*)` returns 29 | |
| t2 | | `COUNT(*)` returns 29 |
| t3 | 29 < 30, insert | |
| t4 | | 29 < 30, insert |
| t5 | commit | commit, the seminar now has 31 enrollments |

**Duplicate enrollment.** The same rookie sends two requests. Both run `existsBySeminarIdAndRookieId` before either inserts, so both see "not enrolled" and both try to insert.

**Duplicate cancellation.** The same rookie cancels twice. Both requests find the enrollment, both call `DELETE`, and both return `204`, although only one row was removed.

## 2. Why a transaction alone does not prevent it

MySQL InnoDB defaults to `REPEATABLE READ`. A plain `SELECT` is a consistent snapshot read and takes no locks. Two transactions can read the same count and the same "not enrolled" state, and neither blocks the other. Both decisions are correct for the snapshot they saw. The snapshot is stale by the time they write.

A transaction gives atomicity, which means all of a transaction's writes commit together or not at all. It does not serialize the decisions of concurrent transactions. The application has to make conflicting requests take turns, or make the database reject the losing write.

## 3. The solution

### Pessimistic lock on the seminar row

Every operation that changes a seminar's enrollments, or the records attached to them, starts by locking the seminar row:

```sql
SELECT * FROM seminar WHERE id = :id FOR UPDATE
```

| Operation | Service method | What the lock serializes |
| --- | --- | --- |
| Enroll | `EnrollmentService.enroll` | Capacity check, duplicate check, insert |
| Cancel | `EnrollmentService.cancel` | Lookup and delete of the rookie's enrollment |
| Update enrollment | `EnrollmentService.updateEnrollment` | Read, validate, and save of grace days and the dropped flag |
| Batch record update | `SessionRecordService.updateRecords` | Enrollment validation and the record upserts |

All checks run after the lock is acquired and inside the same `@Transactional` method. The lock is held until commit or rollback. Each request therefore sees the result of the previous request on the same seminar:

- The 31st enrollment reads a count of 30 and gets `409`.
- The second identical enrollment sees the first one and gets `409`.
- The second identical cancellation finds no enrollment and gets `404`.

Requests for different seminars lock different rows and do not wait for each other.

### UNIQUE constraint as the last line of defense

`enrollment` has `UNIQUE (seminar_id, rookie_id)` (`V5__create_enrollment.sql`). The lock already prevents duplicate inserts. The constraint guarantees the invariant even if a future code path forgets to take the lock.

A constraint violation is deliberately not translated into `409`. If it ever happens, the locking is broken, and a `500` makes that visible instead of hiding it as a normal conflict.

### Headcount from `COUNT(*)`, not a stored column

The current headcount is always `SELECT COUNT(*) FROM enrollment WHERE seminar_id = ?`. No `enrolled_count` column exists, so there is no second value that can drift from the real rows. Failed enrollments, duplicate requests, failed cancellations, and rollbacks cannot leave a wrong counter behind.

## 4. Why this approach

| Approach | Pros | Cons | Decision |
| --- | --- | --- | --- |
| Pessimistic lock (`FOR UPDATE`) on the seminar row | Simple and correct. No retries. Every request gets a definite answer on the first try. | Requests for the same seminar wait in line. | **Chosen** |
| Optimistic lock (version column) with retry | No waiting while there are no conflicts. | Under a 100-on-30 race, most requests conflict. The retry loop must keep going until there are no free seats, or the spec's `min(N, M)` successes is violated. Needs a new column and retry code. | Rejected |
| Conditional atomic update on an `enrolled_count` column | One statement, and the row lock is held very briefly. | Adds a counter that has to be backfilled and kept in sync on enroll, cancel, and rollback. The duplicate check still needs the UNIQUE constraint. | Rejected |
| UNIQUE constraint only | Prevents duplicate enrollment. | Does nothing for capacity. | Kept as a safety net only |

Contention is limited to one seminar's enrollment window. Holding a row lock for a few short queries is cheap at this scale, and the code stays a straightforward read-check-write. The spec also requires that free seats are never lost to lock conflicts. A blocking lock satisfies that without any retry logic.

## 5. Transaction boundaries

Each item below runs in one `@Transactional` service method. Either all of its DB changes commit, or none do.

**Cancel enrollment** (`EnrollmentService.cancel`)

1. Lock the seminar row.
2. Delete the enrollment row.
3. Delete the `session_record` rows of that enrollment, through `ON DELETE CASCADE` on `fk_session_record_enrollment` (`V6__create_session_record.sql`).

The headcount needs no update because it is derived from the rows. The cascade runs inside the same statement as the delete, so records cannot outlive their enrollment. Other rookies' records and the seminar's sessions are untouched.

**Batch record update** (`SessionRecordService.updateRecords`)

1. Lock the seminar row.
2. Verify that every `enrollmentId` belongs to the session's seminar.
3. For each item, run an `INSERT ... ON DUPLICATE KEY UPDATE` upsert that writes only the fields that were sent.
4. For each item, delete the row if both fields are now `NULL`.

A failure at any point rolls back every upsert and delete already executed. All business exceptions extend `ApiException`, which extends `RuntimeException`. Spring rolls back on `RuntimeException` by default, so no `rollbackFor` is needed.

**Update enrollment** (`EnrollmentService.updateEnrollment`)

1. Lock the seminar row.
2. Load the enrollment and check that it belongs to the seminar.
3. Validate `graceDaysRemaining` against the seminar's `totalGraceDays`.
4. Save both fields in one `UPDATE`.

All input is validated before the write, so a request with one invalid field changes nothing.

## 6. Verification

Run every test with Docker running. Testcontainers starts a MySQL 8.4 instance.

```bash
./gradlew test
```

Run only the team tests:

```bash
./gradlew test --tests 'com.wafflestudio.spring2026.team.*'
```

### Concurrency tests

`src/test/kotlin/com/wafflestudio/spring2026/team/EnrollmentConcurrencyTeamTest.kt`

Each test prepares its own seminar and users before the concurrent phase. A fixed pool of 32 threads waits on a ready latch sized to the worker count, then a start latch releases all workers together. Every `Future` is collected, so an exception in a worker fails the test. Final state is read through a fresh `JdbcClient` query outside any test transaction, and is also compared with `GET /seminars/{id}`.

| Scenario | Checks |
| --- | --- |
| 100 rookies race for 30 seats | Exactly 30 `201` and 70 `409`. 30 rows with 30 distinct rookies. `enrolledCount = 30`, `status = CLOSED`. |
| One rookie enrolls 20 times | One `201` and 19 `409`. One row for that rookie. |
| One rookie cancels 10 times | One `204` and 9 `404`. That rookie's row is gone, and the headcount dropped by exactly one. |
| Full seminar (5): 5 cancellations and 10 new enrollments, shuffled | Every cancellation `204`, every enrollment `201` or `409`. `final = initial + enrolled - cancelled` and `0 ≤ final ≤ 5`. The API matches the DB. Free seats can be filled afterwards, and the next enrollment gets `409`. |

Any other status, such as a `500`, makes the tally assertion fail. It is never counted as a capacity or duplicate conflict.

### Rollback test after a real DB write

`src/test/kotlin/com/wafflestudio/spring2026/team/SessionRecordsRollbackTest.kt`

1. Commit the initial records: `first = (PRESENT, PASSED)`, `second = (ABSENT, null)`, and no row for `third`.
2. Replace `SessionRecordRepository` with `FailingSessionRecordRepository`, a `@Primary` test bean that delegates to the real Spring Data repository.
3. Call the real `SessionRecordService.updateRecords`. The wrapper throws `InjectedFailure` after N real upserts.
4. Before throwing, the wrapper reads the touched rows inside the failing transaction. This snapshot proves that the write reached MySQL, for example `first` already changed to `(ABSENT, null)`.
5. Assert that the exception reaches the caller. Then read `session_record` in a new connection and assert that it equals the initial state.

The test covers failure after the first write, and failure after a create, an update, and a delete have all executed. A control case without failure injection shows that the same request is fully applied.

### Other team regression tests

- `team/EnrollmentUpdateTeamTest.kt`: partial updates, permissions, cross-seminar `404`, and record cleanup and fresh state on re-enrollment after a cancellation.
- `team/dashboard/TeamDashboardRegressionTest.kt`: dashboard permissions, dropped rookies, record placement by session ID, and paging limits.

## Schema changes

Week 4 added no migration. The constraints this design depends on already exist:

- `uk_enrollment_seminar_rookie`: `UNIQUE (seminar_id, rookie_id)` in `V5__create_enrollment.sql`.
- `ck_enrollment_grace_days`: `CHECK (grace_days_remaining >= 0)` in `V5__create_enrollment.sql`.
- `uk_session_record_session_enrollment`: `UNIQUE (session_id, enrollment_id)` in `V6__create_session_record.sql`. One record per session and enrollment, which the upsert relies on.
- `fk_session_record_enrollment ... ON DELETE CASCADE` in `V6__create_session_record.sql`. Cancelling an enrollment removes its records.

Because the headcount is computed with `COUNT(*)`, there is no counter column to backfill for existing data.
