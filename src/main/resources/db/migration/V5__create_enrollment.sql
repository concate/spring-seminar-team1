-- 수강 신청. "이 루키가 이 세미나를 수강하는 것" 이므로 잔여 Grace Day 와 탈락 여부를 여기에 둔다.
-- 취소하면 행을 삭제한다. 세미나의 현재 신청 인원은 COUNT(*) WHERE seminar_id = ? 로 구한다.
-- 잔여 Grace Day 의 상한(세미나의 total_grace_days)은 다른 테이블 값이라 애플리케이션에서 검증한다.
CREATE TABLE enrollment
(
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    seminar_id           BIGINT      NOT NULL,
    rookie_id            BIGINT      NOT NULL,
    grace_days_remaining INT         NOT NULL,
    dropped              BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at           DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT uk_enrollment_seminar_rookie UNIQUE (seminar_id, rookie_id),
    CONSTRAINT fk_enrollment_seminar FOREIGN KEY (seminar_id) REFERENCES seminar (id),
    CONSTRAINT fk_enrollment_rookie FOREIGN KEY (rookie_id) REFERENCES user (id),
    CONSTRAINT ck_enrollment_grace_days CHECK (grace_days_remaining >= 0),
    INDEX idx_enrollment_rookie (rookie_id)
);
