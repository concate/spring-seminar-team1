package com.wafflestudio.spring2026.user.model

import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("user")
data class User(
    @Id
    val id: Long? = null,
    val email: String,
    val password: String,
    val name: String,
    val githubUsername: String,
    val role: Role,
    val status: UserStatus,
    val seminarId: Long?,
    val createdAt: Instant,
)
