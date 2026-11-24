package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.InvalidRecordsRequestException
import com.wafflestudio.spring2026.session.model.AssignmentStatus
import com.wafflestudio.spring2026.session.model.Attendance
import com.wafflestudio.spring2026.session.model.RecordChange
import org.openapitools.jackson.nullable.JsonNullable
import tools.jackson.databind.JsonNode

object SessionRecordsRequest {
    fun parse(body: JsonNode?): List<RecordChange> {
        if (body == null || !body.isObject) throw InvalidRecordsRequestException("Request body must be an object.")

        val records = body.get("records")
        if (records == null || !records.isArray || records.isEmpty) {
            throw InvalidRecordsRequestException("records must be a non-empty array.")
        }

        val changes = records.values().map(::parseRecord)
        if (changes.map { it.enrollmentId }.toSet().size != changes.size) {
            throw InvalidRecordsRequestException("enrollmentId must not be duplicated.")
        }

        return changes
    }

    private fun parseRecord(node: JsonNode): RecordChange {
        if (!node.isObject) throw InvalidRecordsRequestException("Each record must be an object.")

        val enrollmentId = node.get("enrollmentId")
        if (enrollmentId == null || !enrollmentId.isIntegralNumber || !enrollmentId.canConvertToLong() || enrollmentId.longValue() <= 0) {
            throw InvalidRecordsRequestException("enrollmentId must be a positive integer.")
        }

        val attendance = parseField(node, "attendance", Attendance::valueOf, Attendance.entries.map { it.name })
        val assignmentStatus = parseField(node, "assignmentStatus", AssignmentStatus::valueOf, AssignmentStatus.entries.map { it.name })
        if (!attendance.isPresent && !assignmentStatus.isPresent) {
            throw InvalidRecordsRequestException("Each record must include attendance or assignmentStatus.")
        }

        return RecordChange(
            enrollmentId = enrollmentId.longValue(),
            attendance = attendance,
            assignmentStatus = assignmentStatus,
        )
    }

    private fun <T> parseField(
        node: JsonNode,
        name: String,
        parse: (String) -> T,
        allowed: List<String>,
    ): JsonNullable<T?> {
        if (!node.has(name)) return JsonNullable.undefined()

        val value = node.get(name)
        if (value.isNull) return JsonNullable.of(null)
        if (!value.isString || value.stringValue() !in allowed) {
            throw InvalidRecordsRequestException("$name must be one of $allowed or null.")
        }

        return JsonNullable.of(parse(value.stringValue()))
    }
}
