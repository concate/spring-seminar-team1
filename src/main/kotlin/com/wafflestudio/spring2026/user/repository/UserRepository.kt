package com.wafflestudio.spring2026.user.repository

import com.wafflestudio.spring2026.user.model.User
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository : CrudRepository<User, Long> {
    fun existsByEmail(email: String): Boolean

    @Query(
        """
        SELECT * FROM `user`
        WHERE role <> 'ADMIN'
          AND (:status IS NULL OR status = :status)
          AND (:role IS NULL OR role = :role)
        ORDER BY created_at DESC, id DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun findApplicants(
        @Param("status") status: String?,
        @Param("role") role: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<User>

    @Query(
        """
        SELECT COUNT(*) FROM `user`
        WHERE role <> 'ADMIN'
          AND (:status IS NULL OR status = :status)
          AND (:role IS NULL OR role = :role)
        """,
    )
    fun countApplicants(
        @Param("status") status: String?,
        @Param("role") role: String?,
    ): Long
}
