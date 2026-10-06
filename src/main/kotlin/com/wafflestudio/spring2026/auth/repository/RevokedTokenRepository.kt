package com.wafflestudio.spring2026.auth.repository

import java.time.Instant
import java.time.ZoneOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

// 로그아웃한 토큰의 jti 를 저장한다. 서버가 재시작되어도 무효 상태가 유지되도록 DB 에 둔다.
@Repository
class RevokedTokenRepository(
    private val jdbcClient: JdbcClient,
) {
    fun save(
        jti: String,
        expiresAt: Instant,
    ) {
        jdbcClient
            .sql("INSERT INTO revoked_token (jti, expires_at) VALUES (:jti, :expiresAt)")
            .param("jti", jti)
            .param("expiresAt", expiresAt.atOffset(ZoneOffset.UTC).toLocalDateTime())
            .update()
    }

    fun existsByJti(jti: String): Boolean =
        jdbcClient
            .sql("SELECT COUNT(*) FROM revoked_token WHERE jti = :jti")
            .param("jti", jti)
            .query(Long::class.java)
            .single() > 0
}
