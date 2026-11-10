package com.wafflestudio.spring2026.enrollment

import com.wafflestudio.spring2026.ApiException
import org.springframework.http.HttpStatus

class EnrollmentNotFoundException(
    seminarId: Long,
) : ApiException(HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "세미나 ${seminarId}에 신청한 내역이 없습니다.")

class SeminarNotOpenException :
    ApiException(HttpStatus.CONFLICT, "SEMINAR_NOT_OPEN", "신청 중인 세미나가 아닙니다.")

class AlreadyEnrolledException :
    ApiException(HttpStatus.CONFLICT, "ALREADY_ENROLLED", "이미 신청한 세미나입니다.")

class NotInApplyPeriodException :
    ApiException(HttpStatus.CONFLICT, "NOT_IN_APPLY_PERIOD", "신청 기간에만 수강 신청을 취소할 수 있습니다.")
