-- 세미나. 와장만 개설하며 삭제되지 않는다. 수정 가능한 것은 title, description 뿐이다.
-- 현재 신청 인원(enrolledCount)은 저장하지 않고 enrollment 를 세서 구한다.
-- 일시는 모두 UTC 로 저장한다.
CREATE TABLE seminar
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(255) NOT NULL,
    description      TEXT         NULL,
    capacity         INT          NOT NULL,
    apply_start_at   DATETIME(6)  NOT NULL,
    apply_end_at     DATETIME(6)  NOT NULL,
    total_grace_days INT          NOT NULL,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT ck_seminar_capacity CHECK (capacity >= 1),
    CONSTRAINT ck_seminar_total_grace_days CHECK (total_grace_days >= 0),
    CONSTRAINT ck_seminar_apply_period CHECK (apply_end_at > apply_start_at)
);
