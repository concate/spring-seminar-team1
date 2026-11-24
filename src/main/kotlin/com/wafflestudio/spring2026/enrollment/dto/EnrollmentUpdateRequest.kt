package com.wafflestudio.spring2026.enrollment.dto

import com.wafflestudio.spring2026.enrollment.InvalidEnrollmentRequestException
import com.wafflestudio.spring2026.enrollment.model.EnrollmentChange
import tools.jackson.databind.JsonNode

object EnrollmentUpdateRequest {
    fun parse(body: JsonNode?): EnrollmentChange {
        if (body == null || !body.isObject) throw InvalidEnrollmentRequestException("Request body must be an object.")

        val graceDaysRemaining = body.get("graceDaysRemaining")?.let { node ->
            if (!node.isIntegralNumber || !node.canConvertToInt() || node.intValue() < 0) {
                throw InvalidEnrollmentRequestException("graceDaysRemaining must be a non-negative integer.")
            }
            node.intValue()
        }
        val dropped = body.get("dropped")?.let { node ->
            if (!node.isBoolean) throw InvalidEnrollmentRequestException("dropped must be a boolean.")
            node.booleanValue()
        }
        if (graceDaysRemaining == null && dropped == null) {
            throw InvalidEnrollmentRequestException("Either graceDaysRemaining or dropped is required.")
        }

        return EnrollmentChange(graceDaysRemaining, dropped)
    }
}
