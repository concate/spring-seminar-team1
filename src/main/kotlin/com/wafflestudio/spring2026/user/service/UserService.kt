package com.wafflestudio.spring2026.user.service

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.PageResponse
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.auth.ForbiddenException
import com.wafflestudio.spring2026.user.InvalidApplicantFilterException
import com.wafflestudio.spring2026.user.InvalidApprovalStatusException
import com.wafflestudio.spring2026.user.UserNotFoundException
import com.wafflestudio.spring2026.user.UserNotPendingException
import com.wafflestudio.spring2026.user.model.Role
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.model.UserStatus
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUser(
        loginUser: AuthUser,
        userId: Long,
    ): User {
        loginUser.requireApproved()
        return findUser(userId)
    }

    fun getMe(loginUser: AuthUser): User = findUser(loginUser.id)

    fun getApplicants(
        loginUser: AuthUser,
        status: UserStatus?,
        role: Role?,
        pageQuery: PageQuery,
    ): PageResponse<User> {
        loginUser.requireAdminOrStaff()
        if (role == Role.ADMIN) throw InvalidApplicantFilterException()

        val visibleRole = if (loginUser.isAdmin) role else Role.ROOKIE
        val content = userRepository.findApplicants(
            status = status?.name,
            role = visibleRole?.name,
            limit = pageQuery.size,
            offset = pageQuery.offset,
        )
        val totalElements = userRepository.countApplicants(
            status = status?.name,
            role = visibleRole?.name,
        )

        return pageQuery.toResponse(content, totalElements)
    }

    fun review(
        loginUser: AuthUser,
        userId: Long,
        status: UserStatus,
    ): User {
        loginUser.requireAdminOrStaff()
        if (status == UserStatus.PENDING) throw InvalidApprovalStatusException()

        val user = findUser(userId)
        if (!loginUser.isAdmin && user.role != Role.ROOKIE) {
            throw ForbiddenException("운영진은 루키의 가입 신청만 심사할 수 있습니다.")
        }
        if (user.status != UserStatus.PENDING) throw UserNotPendingException(userId)

        return userRepository.save(user.copy(status = status))
    }

    private fun findUser(userId: Long): User =
        userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException(userId)
}
