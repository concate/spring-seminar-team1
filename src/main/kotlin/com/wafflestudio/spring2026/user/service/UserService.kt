package com.wafflestudio.spring2026.user.service

import com.wafflestudio.spring2026.user.InvalidApprovalStatusException
import com.wafflestudio.spring2026.user.UserNotFoundException
import com.wafflestudio.spring2026.user.UserNotPendingException
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.model.UserStatus
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUser(userId: Long): User =
        userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException(userId)

    fun review(
        userId: Long,
        status: UserStatus,
    ): User {
        if (status == UserStatus.PENDING) throw InvalidApprovalStatusException()

        val user = getUser(userId)
        if (user.status != UserStatus.PENDING) throw UserNotPendingException(userId)

        return userRepository.save(user.copy(status = status))
    }
}
