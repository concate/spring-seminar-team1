-- 회차별 출석·과제 기록. 어떤 회차(session)의, 어떤 수강(enrollment)에 대한 기록이다.
-- 한 회차에서 한 수강생의 기록은 하나뿐이므로 (session_id, enrollment_id) 가 유일하다.
-- 아직 기록하지 않은 칸은 NULL(빈칸)이다. 행이 없는 것도 두 칸 모두 빈칸으로 본다.
-- 수강 신청을 취소해 enrollment 가 지워지면 기록도 함께 지운다.
CREATE TABLE session_record
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    session_id        BIGINT      NOT NULL,
    enrollment_id     BIGINT      NOT NULL,
    attendance_status VARCHAR(20) NULL,
    assignment_status VARCHAR(30) NULL,
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT uk_session_record_session_enrollment UNIQUE (session_id, enrollment_id),
    CONSTRAINT fk_session_record_session FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT fk_session_record_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollment (id) ON DELETE CASCADE,
    CONSTRAINT ck_session_record_attendance CHECK (attendance_status IN ('PRESENT', 'ABSENT')),
    CONSTRAINT ck_session_record_assignment CHECK (assignment_status IN ('PASSED', 'SUBMITTED_BUT_FAILED', 'NOT_SUBMITTED')),
    INDEX idx_session_record_enrollment (enrollment_id)
);
