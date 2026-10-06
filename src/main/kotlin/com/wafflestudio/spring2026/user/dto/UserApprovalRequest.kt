package com.wafflestudio.spring2026.user.dto

import com.wafflestudio.spring2026.user.model.UserStatus
import jakarta.validation.constraints.NotNull

data class UserApprovalRequest(
    @field:NotNull(message = "심사 결과는 비어 있을 수 없습니다.")
    val status: UserStatus?,
)
