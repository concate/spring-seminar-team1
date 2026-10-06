package com.wafflestudio.spring2026.enrollment

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class EnrollmentNotFoundException(
    enrollmentId: Long,
) : ApiException(HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "ID가 ${enrollmentId}인 수강 신청을 찾을 수 없습니다.")

class NotRookieException :
    ApiException(HttpStatus.BAD_REQUEST, "NOT_ROOKIE", "루키만 수강 신청할 수 있습니다.")

class RookieNotApprovedException :
    ApiException(HttpStatus.FORBIDDEN, "ROOKIE_NOT_APPROVED", "가입이 승인된 루키만 수강 신청할 수 있습니다.")

class SeminarNotOpenException :
    ApiException(HttpStatus.CONFLICT, "SEMINAR_NOT_OPEN", "신청 중인 세미나가 아닙니다.")

class AlreadyEnrolledException :
    ApiException(HttpStatus.CONFLICT, "ALREADY_ENROLLED", "이미 신청한 세미나입니다.")

class NotInApplyPeriodException :
    ApiException(HttpStatus.CONFLICT, "NOT_IN_APPLY_PERIOD", "신청 기간에만 수강 신청을 취소할 수 있습니다.")
