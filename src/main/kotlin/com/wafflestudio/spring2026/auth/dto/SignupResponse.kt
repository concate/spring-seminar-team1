package com.wafflestudio.spring2026.auth.dto

import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.model.UserStatus
import java.time.OffsetDateTime
import java.time.ZoneId

data class SignupResponse(
    val id: Long,
    val status: UserStatus,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(user: User) =
            SignupResponse(
                id = user.id!!,
                status = user.status,
                createdAt = user.createdAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
            )
    }
}
