package com.wafflestudio.spring2026.user.dto

import com.wafflestudio.spring2026.user.model.Role
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.model.UserStatus
import java.time.OffsetDateTime
import java.time.ZoneId

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val githubUsername: String,
    val role: Role,
    val status: UserStatus,
    val seminarId: Long?,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(user: User) =
            UserResponse(
                id = user.id!!,
                email = user.email,
                name = user.name,
                githubUsername = user.githubUsername,
                role = user.role,
                status = user.status,
                seminarId = user.seminarId,
                createdAt = user.createdAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
            )
    }
}
