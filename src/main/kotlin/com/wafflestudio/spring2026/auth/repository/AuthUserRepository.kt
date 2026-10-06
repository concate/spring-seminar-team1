package com.wafflestudio.spring2026.auth.repository

import com.wafflestudio.spring2026.auth.AuthUser
import java.sql.ResultSet
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class AuthUserRepository(
    private val jdbcClient: JdbcClient,
) {
    fun findById(id: Long): AuthUser? =
        jdbcClient
            .sql("SELECT id, email, password, role, status, seminar_id FROM `user` WHERE id = :id")
            .param("id", id)
            .query { rs, _ -> rs.toAuthUser() }
            .optional()
            .orElse(null)

    fun findByEmail(email: String): AuthUser? =
        jdbcClient
            .sql("SELECT id, email, password, role, status, seminar_id FROM `user` WHERE email = :email")
            .param("email", email)
            .query { rs, _ -> rs.toAuthUser() }
            .optional()
            .orElse(null)

    private fun ResultSet.toAuthUser() =
        AuthUser(
            id = getLong("id"),
            email = getString("email"),
            password = getString("password"),
            role = getString("role"),
            status = getString("status"),
            seminarId = getObject("seminar_id", java.lang.Long::class.java)?.toLong(),
        )
}
