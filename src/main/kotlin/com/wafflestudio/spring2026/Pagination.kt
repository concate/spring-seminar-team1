package com.wafflestudio.spring2026

import org.springframework.http.HttpStatus

data class PageQuery(
    val page: Int,
    val size: Int,
) {
    val offset: Long get() = page.toLong() * size

    fun <T> toResponse(
        content: List<T>,
        totalElements: Long,
    ): PageResponse<T> =
        PageResponse(
            content = content,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = ((totalElements + size - 1) / size).toInt(),
        )

    companion object {
        const val DEFAULT_SIZE = 20
        const val MAX_SIZE = 100

        fun of(
            page: Int?,
            size: Int?,
        ): PageQuery {
            val resolvedPage = page ?: 0
            val resolvedSize = size ?: DEFAULT_SIZE
            if (resolvedPage < 0) throw InvalidPageRequestException("page 는 0 이상이어야 합니다.")
            if (resolvedSize !in 1..MAX_SIZE) throw InvalidPageRequestException("size 는 1 이상 $MAX_SIZE 이하여야 합니다.")
            return PageQuery(resolvedPage, resolvedSize)
        }
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

class InvalidPageRequestException(
    message: String,
) : ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message)
