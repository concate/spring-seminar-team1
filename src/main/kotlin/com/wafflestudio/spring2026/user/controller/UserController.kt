package com.wafflestudio.spring2026.user.controller

import com.wafflestudio.spring2026.user.dto.UserApprovalRequest
import com.wafflestudio.spring2026.user.dto.UserResponse
import com.wafflestudio.spring2026.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{userId}")
    fun getUser(
        @PathVariable userId: Long,
    ): ResponseEntity<UserResponse> = ResponseEntity.ok(UserResponse.from(userService.getUser(userId)))

    @PatchMapping("/{userId}/approval")
    fun review(
        @PathVariable userId: Long,
        @Valid @RequestBody request: UserApprovalRequest,
    ): ResponseEntity<UserResponse> {
        val user = userService.review(
            userId = userId,
            status = request.status!!,
        )

        return ResponseEntity.ok(UserResponse.from(user))
    }
}
