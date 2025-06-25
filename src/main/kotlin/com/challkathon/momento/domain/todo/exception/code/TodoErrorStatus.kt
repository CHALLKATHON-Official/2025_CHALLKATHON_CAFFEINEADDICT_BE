package com.challkathon.momento.domain.todo.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class TodoErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {

    // Todo 조회 관련
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO_001", "Todo를 찾을 수 없습니다."),
    TODO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "TODO_002", "해당 Todo에 접근할 권한이 없습니다."),

    // Todo 완료 관련
    TODO_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "TODO_003", "이미 완료된 Todo입니다."),
    TODO_COMPLETION_MEMO_REQUIRED(HttpStatus.BAD_REQUEST, "TODO_004", "완료 메모는 필수입니다."),
    TODO_COMPLETION_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "TODO_005", "인증샷 이미지는 필수입니다."),

    // 파일 업로드 관련
    TODO_IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "TODO_006", "파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다."),
    TODO_IMAGE_TYPE_NOT_SUPPORTED(
        HttpStatus.BAD_REQUEST,
        "TODO_007",
        "지원하지 않는 파일 형식입니다. 이미지 파일(JPG, PNG, GIF, WEBP)만 업로드 가능합니다."
    ),
    TODO_IMAGE_FILENAME_INVALID(HttpStatus.BAD_REQUEST, "TODO_008", "파일명이 올바르지 않습니다."),
    TODO_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "TODO_009", "빈 파일은 업로드할 수 없습니다."),
    TODO_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TODO_010", "인증샷 업로드에 실패했습니다. 다시 시도해주세요.");

    private val isSuccess = false

    override fun getCode(): BaseCode {
        return BaseCode(
            httpStatus = httpStatus,
            isSuccess = isSuccess,
            code = code,
            message = message
        )
    }
}