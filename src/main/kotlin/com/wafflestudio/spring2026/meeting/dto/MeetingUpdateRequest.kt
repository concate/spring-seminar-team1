package com.wafflestudio.spring2026.meeting.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class MeetingUpdateRequest(
    // 값을 보내지 않으면(null) 기존 값을 유지하므로, 보낸 경우에만 공백 여부를 검증합니다.
    @field:Pattern(regexp = "(?s).*\\S.*", message = "모임 제목은 비어 있을 수 없습니다.")
    val title: String? = null,

    @field:Positive(message = "모임 정원은 1명 이상이어야 합니다.")
    val capacity: Int? = null,
)
