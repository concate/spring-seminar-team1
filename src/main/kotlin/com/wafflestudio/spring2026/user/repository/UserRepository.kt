package com.wafflestudio.spring2026.user.repository

import com.wafflestudio.spring2026.user.model.User
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {
    fun existsByEmail(email: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM seminar WHERE id = :seminarId)")
    fun existsSeminarById(seminarId: Long): Boolean
}
