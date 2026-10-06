-- 수업 회차. 회차마다 과제가 항상 하나 있으므로(1:1) 과제 제목과 설명을 같은 행에 둔다.
-- 회차 번호(round)는 저장하지 않는다. 세미나 안에서 (starts_at, id) 오름차순으로 정렬한 순서로 계산한다.
CREATE TABLE session
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    seminar_id         BIGINT       NOT NULL,
    title              VARCHAR(255) NOT NULL,
    starts_at          DATETIME(6)  NOT NULL,
    location           VARCHAR(255) NOT NULL,
    lecture_content    TEXT         NULL,
    assignment_title   VARCHAR(255) NOT NULL,
    assignment_content TEXT         NULL,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_session_seminar FOREIGN KEY (seminar_id) REFERENCES seminar (id),
    INDEX idx_session_seminar_round (seminar_id, starts_at, id)
);
