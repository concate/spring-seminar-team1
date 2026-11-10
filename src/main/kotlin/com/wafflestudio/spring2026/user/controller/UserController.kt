package com.wafflestudio.spring2026.user.controller

import com.wafflestudio.spring2026.PageQuery
import com.wafflestudio.spring2026.PageResponse
import com.wafflestudio.spring2026.auth.AuthUser
import com.wafflestudio.spring2026.auth.LoginUser
import com.wafflestudio.spring2026.user.dto.UserApprovalRequest
import com.wafflestudio.spring2026.user.dto.UserResponse
import com.wafflestudio.spring2026.user.model.Role
import com.wafflestudio.spring2026.user.model.UserStatus
import com.wafflestudio.spring2026.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    fun getApplicants(
        @LoginUser loginUser: AuthUser,
        @RequestParam(required = false) status: UserStatus?,
        @RequestParam(required = false) role: Role?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ResponseEntity<PageResponse<UserResponse>> {
        val result = userService.getApplicants(
            loginUser = loginUser,
            status = status,
            role = role,
            pageQuery = PageQuery.of(page, size),
        )

        return ResponseEntity.ok(result.map(UserResponse::from))
    }

    @GetMapping("/me")
    fun getMe(
        @LoginUser loginUser: AuthUser,
    ): ResponseEntity<UserResponse> = ResponseEntity.ok(UserResponse.from(userService.getMe(loginUser)))

    @GetMapping("/{userId}")
    fun getUser(
        @LoginUser loginUser: AuthUser,
        @PathVariable userId: Long,
    ): ResponseEntity<UserResponse> = ResponseEntity.ok(UserResponse.from(userService.getUser(loginUser, userId)))

    @PatchMapping("/{userId}/approval")
    fun review(
        @LoginUser loginUser: AuthUser,
        @PathVariable userId: Long,
        @Valid @RequestBody request: UserApprovalRequest,
    ): ResponseEntity<UserResponse> {
        val user = userService.review(
            loginUser = loginUser,
            userId = userId,
            status = request.status!!,
        )

        return ResponseEntity.ok(UserResponse.from(user))
    }
}
