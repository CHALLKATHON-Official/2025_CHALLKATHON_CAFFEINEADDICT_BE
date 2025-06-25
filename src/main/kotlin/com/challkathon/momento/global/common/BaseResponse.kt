package com.challkathon.momento.global.common

import com.challkathon.momento.global.exception.code.BaseCodeInterface
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonPropertyOrder("timestamp", "code", "message", "result")
@Schema(description = "공통 응답 DTO")
data class BaseResponse<T>(
    @Schema(description = "응답 시간", example = "2021-07-01T00:00:00")
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Schema(description = "응답 코드", example = "200")
    val code: String,

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    val message: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "응답 데이터")
    val result: T?
) {
    companion object {
        // 성공한 경우 응답 생성
        fun <T> onSuccess(result: T): BaseResponse<T> {
            return BaseResponse(
                code = "COMMON200",
                message = "요청에 성공하였습니다.",
                result = result
            )
        }

        // 생성 요청 성공 시 응답 생성
        fun <T> onSuccessCreate(result: T): BaseResponse<T> {
            return BaseResponse(
                code = "COMMON201",
                message = "요청에 성공하였습니다.",
                result = result
            )
        }

        // 삭제 요청 성공 시 응답 생성
        fun <T> onSuccessDelete(result: T): BaseResponse<T> {
            return BaseResponse(
                code = "COMMON202",
                message = "삭제 요청에 성공하였습니다.",
                result = result
            )
        }

        // 공통 코드를 사용하여 응답 생성
        fun <T> of(code: BaseCodeInterface, result: T): BaseResponse<T> {
            return BaseResponse(
                code = code.getCode().code,
                message = code.getCode().message,
                result = result
            )
        }

        // 실패한 경우 응답 생성
        fun <T> onFailure(code: String, message: String, result: T?): BaseResponse<T> {
            return BaseResponse(
                code = code,
                message = message,
                result = result
            )
        }
    }
}