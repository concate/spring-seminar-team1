package com.wafflestudio.spring2026.auth.service

import com.wafflestudio.spring2026.auth.UnauthorizedException
import com.wafflestudio.spring2026.auth.dto.SignupRequest
import com.wafflestudio.spring2026.auth.repository.AuthUserRepository
import com.wafflestudio.spring2026.auth.repository.RevokedTokenRepository
import com.wafflestudio.spring2026.seminar.SeminarNotFoundException
import com.wafflestudio.spring2026.user.DuplicateEmailException
import com.wafflestudio.spring2026.user.InvalidSignupException
import com.wafflestudio.spring2026.user.model.Role
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.model.UserStatus
import com.wafflestudio.spring2026.user.repository.UserRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authUserRepository: AuthUserRepository,
    private val revokedTokenRepository: RevokedTokenRepository,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
) {
    fun signup(request: SignupRequest): User {
        val role = request.role!!
        when (role) {
            Role.ADMIN -> throw InvalidSignupException("와장으로는 가입할 수 없습니다.")
            Role.STAFF -> if (request.seminarId == null) throw InvalidSignupException("운영진은 담당 세미나를 선택해야 합니다.")
            Role.ROOKIE -> if (request.seminarId != null) throw InvalidSignupException("루키는 세미나를 선택하지 않습니다.")
        }
        if (request.seminarId != null && !userRepository.existsSeminarById(request.seminarId)) {
            throw SeminarNotFoundException(request.seminarId)
        }
        if (userRepository.existsByEmail(request.email)) throw DuplicateEmailException(request.email)

        return userRepository.save(
            User(
                email = request.email,
                password = request.password,
                name = request.name,
                githubUsername = request.githubUsername,
                role = role,
                status = UserStatus.PENDING,
                seminarId = request.seminarId,
                createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
            ),
        )
    }

    fun login(
        email: String,
        password: String,
    ): String {
        val user = authUserRepository.findByEmail(email)
        if (user == null || !passwordMatches(password, user.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        return tokenProvider.issue(user.id)
    }

    fun logout(authorization: String?) {
        val claims = tokenProvider.verify(authorization)
        try {
            revokedTokenRepository.save(claims.revocationKey, claims.expiresAt)
        } catch (e: DuplicateKeyException) {
            throw UnauthorizedException("로그아웃된 토큰입니다.")
        }
    }

    // TODO: 지금은 평문 비교. 해싱을 붙일 때 이 함수와 가입 시 저장 부분만 바꾼다.
    private fun passwordMatches(
        raw: String,
        stored: String,
    ): Boolean = raw == stored
}
