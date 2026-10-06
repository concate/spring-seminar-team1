package com.wafflestudio.spring2026.auth

import com.wafflestudio.spring2026.auth.repository.AuthUserRepository
import com.wafflestudio.spring2026.auth.service.TokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

// 로그인이 필요한 요청의 토큰을 검증하고, 요청자를 request attribute 에 담는다.
// 컨트롤러가 요청 본문을 읽기 전에 실행되므로, 토큰이 잘못되면 다른 어떤 검사보다 먼저 401 이 된다.
@Component
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val authUserRepository: AuthUserRepository,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == "OPTIONS" || isPublic(request.method, request.requestURI)) return true

        val claims = tokenProvider.verify(request.getHeader(AUTHORIZATION_HEADER))
        val user = authUserRepository.findById(claims.userId)
            ?: throw UnauthorizedException("존재하지 않는 사용자의 토큰입니다.")

        request.setAttribute(LOGIN_USER_ATTRIBUTE, user)
        return true
    }

    // 같은 경로라도 메서드에 따라 공개 여부가 다르므로(GET /seminars 는 공개, POST /seminars 는 아님) 여기서 판단한다.
    private fun isPublic(
        method: String,
        path: String,
    ): Boolean =
        (method == "POST" && path in setOf("/auth/signup", "/auth/login")) ||
            (method == "GET" && path == "/seminars")

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val LOGIN_USER_ATTRIBUTE = "loginUser"
    }
}
