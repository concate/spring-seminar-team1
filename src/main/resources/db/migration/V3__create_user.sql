-- 사용자. 역할(role)은 가입 시 정해지고 바뀌지 않는다.
-- 운영진(STAFF)은 정확히 하나의 세미나를 담당하므로 seminar_id 를 직접 가진다. (세미나 1 : N 운영진)
-- 와장(ADMIN)과 루키(ROOKIE)는 seminar_id 가 없다. 루키의 수강 세미나는 enrollment 로 표현한다.
CREATE TABLE user
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    github_username VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    seminar_id      BIGINT       NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT fk_user_seminar FOREIGN KEY (seminar_id) REFERENCES seminar (id),
    CONSTRAINT ck_user_role CHECK (role IN ('ADMIN', 'STAFF', 'ROOKIE')),
    CONSTRAINT ck_user_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_user_staff_seminar CHECK ((role = 'STAFF') = (seminar_id IS NOT NULL))
);
