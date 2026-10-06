package com.wafflestudio.spring2026.auth

// 컨트롤러 파라미터에 붙이면 로그인한 사용자(AuthUser)를 주입받는다.
// 예: fun createSeminar(@LoginUser user: AuthUser, ...)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginUser
