package com.wafflestudio.spring2026.auth.service

import com.wafflestudio.spring2026.auth.UnauthorizedException
import com.wafflestudio.spring2026.auth.repository.RevokedTokenRepository
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

// 검증 결과. revocationKey 는 로그아웃 시 revoked_token 에 저장하는 값이다.
data class TokenClaims(
    val userId: Long,
    val revocationKey: String,
    val expiresAt: Instant,
)

// JWT(HS256) 를 의존성 없이 직접 발급·검증한다. 3주차 규칙을 따른다.
//   sub = 사용자 ID 문자열, iat = 발급 시각(초), exp = iat + 3600, jti = UUID
// 3주차에 jjwt 가 들어오면 이 클래스 안쪽만 바꾸면 된다.
@Component
class TokenProvider(
    @Value("\${jwt.secret}") secret: String,
    private val objectMapper: ObjectMapper,
    private val revokedTokenRepository: RevokedTokenRepository,
) {
    private val key = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), ALGORITHM)
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun issue(userId: Long): String {
        val issuedAt = Instant.now().epochSecond
        val header = encodeJson(mapOf("alg" to "HS256", "typ" to "JWT"))
        val payload = encodeJson(
            mapOf(
                "sub" to userId.toString(),
                "iat" to issuedAt,
                "exp" to issuedAt + EXPIRES_IN_SECONDS,
                "jti" to UUID.randomUUID().toString(),
            ),
        )
        val signature = encoder.encodeToString(sign("$header.$payload"))

        return "$header.$payload.$signature"
    }

    // Authorization 헤더 값을 받아 검증한다. 실패하면 모두 UnauthorizedException(401).
    fun verify(authorization: String?): TokenClaims {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw UnauthorizedException("Authorization 헤더에 Bearer 토큰이 없습니다.")
        }

        val parts = authorization.removePrefix(BEARER_PREFIX).trim().split('.')
        if (parts.size != 3) throw UnauthorizedException("토큰 형식이 올바르지 않습니다.")
        val (header, payload, signature) = parts

        val actualSignature = runCatching { decoder.decode(signature) }.getOrNull()
        if (actualSignature == null || !MessageDigest.isEqual(sign("$header.$payload"), actualSignature)) {
            throw UnauthorizedException("토큰 서명이 올바르지 않습니다.")
        }
        if (decodeJson(header)["alg"] != "HS256") throw UnauthorizedException("지원하지 않는 토큰 알고리즘입니다.")

        val claims = decodeJson(payload)
        val expiresAt = (claims["exp"] as? Number)?.toLong()
            ?: throw UnauthorizedException("토큰에 만료 시각이 없습니다.")
        if (Instant.now().epochSecond >= expiresAt) throw UnauthorizedException("만료된 토큰입니다.")

        val userId = (claims["sub"] as? String)?.toLongOrNull()
            ?: throw UnauthorizedException("토큰의 사용자 정보가 올바르지 않습니다.")
        // jti 가 없는 토큰도 서명은 토큰마다 다르므로 서명으로 로그아웃 여부를 구분한다.
        val revocationKey = claims["jti"] as? String ?: signature
        if (revokedTokenRepository.existsByJti(revocationKey)) throw UnauthorizedException("로그아웃된 토큰입니다.")

        return TokenClaims(userId, revocationKey, Instant.ofEpochSecond(expiresAt))
    }

    private fun sign(data: String): ByteArray =
        Mac.getInstance(ALGORITHM).apply { init(key) }.doFinal(data.toByteArray(Charsets.UTF_8))

    private fun encodeJson(value: Map<String, Any>): String = encoder.encodeToString(objectMapper.writeValueAsBytes(value))

    private fun decodeJson(part: String): Map<*, *> =
        runCatching { objectMapper.readValue(decoder.decode(part), Map::class.java) }.getOrNull()
            ?: throw UnauthorizedException("토큰 형식이 올바르지 않습니다.")

    companion object {
        private const val ALGORITHM = "HmacSHA256"
        private const val BEARER_PREFIX = "Bearer "
        const val EXPIRES_IN_SECONDS = 3600L
    }
}
