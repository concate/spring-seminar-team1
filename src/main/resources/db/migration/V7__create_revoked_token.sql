-- 로그아웃으로 무효화한 Access Token. 토큰 원문 대신 jti(UUID)를 저장한다.
-- expires_at 이 지난 행은 어차피 만료된 토큰이므로 나중에 지워도 된다.
CREATE TABLE revoked_token
(
    jti        VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (jti),
    INDEX idx_revoked_token_expires_at (expires_at)
);
