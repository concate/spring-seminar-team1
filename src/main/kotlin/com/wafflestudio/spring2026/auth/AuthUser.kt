package com.wafflestudio.spring2026.auth

// 인증·인가에 필요한 사용자 정보만 담은 읽기 모델.
// user 패키지의 User 모델과 별개로 두어, user 기능 구현과 상관없이 auth 가 동작하게 한다.
data class AuthUser(
    val id: Long,
    val email: String,
    val password: String,
    val role: String,
    val status: String,
    val seminarId: Long?,
) {
    val isAdmin: Boolean get() = role == "ADMIN"
    val isApproved: Boolean get() = status == "APPROVED"

    // 아래 require* 는 3주차 인가에서 각 Service 가 가져다 쓴다. 와장은 모든 권한을 가진다.

    fun requireApproved() {
        if (!isAdmin && !isApproved) throw ForbiddenException("가입이 승인되지 않은 사용자입니다.")
    }

    fun requireAdmin() {
        if (!isAdmin) throw ForbiddenException("와장만 사용할 수 있습니다.")
    }

    fun requireRookie() {
        requireApproved()
        if (role != "ROOKIE") throw ForbiddenException("루키만 사용할 수 있습니다.")
    }

    fun requireStaffOf(seminarId: Long) {
        if (isAdmin) return
        requireApproved()
        if (role != "STAFF" || this.seminarId != seminarId) {
            throw ForbiddenException("해당 세미나를 담당하는 운영진만 사용할 수 있습니다.")
        }
    }
}
