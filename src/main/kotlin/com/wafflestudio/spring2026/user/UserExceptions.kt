package com.wafflestudio.spring2026.user

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class UserNotFoundException(
    userId: Long,
) : ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "ID가 ${userId}인 사용자를 찾을 수 없습니다.")

class DuplicateEmailException(
    email: String,
) : ApiException(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 가입한 이메일입니다: $email")

class InvalidSignupException(
    message: String,
) : ApiException(HttpStatus.BAD_REQUEST, "INVALID_SIGNUP", message)

class InvalidApprovalStatusException :
    ApiException(HttpStatus.BAD_REQUEST, "INVALID_APPROVAL_STATUS", "심사 결과는 APPROVED 또는 REJECTED 여야 합니다.")

class UserNotPendingException(
    userId: Long,
) : ApiException(HttpStatus.CONFLICT, "USER_NOT_PENDING", "ID가 ${userId}인 사용자는 이미 심사가 끝났습니다.")
