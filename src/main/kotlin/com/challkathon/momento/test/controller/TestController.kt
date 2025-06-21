package com.challkathon.momento.test.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "테스트 API", description = "인증 테스트를 위한 API")
@RestController
@RequestMapping("/api/v1/test")
class TestController {

    @Operation(summary = "공개 API", description = "인증 없이 접근 가능한 API")
    @GetMapping("/public")
    fun publicEndpoint(): BaseResponse<String> {
        return BaseResponse.onSuccess("공개 API - 누구나 접근 가능합니다")
    }

    @Operation(
        summary = "인증 필요 API",
        description = "로그인한 사용자만 접근 가능한 API",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/authenticated")
    fun authenticatedEndpoint(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): BaseResponse<Map<String, Any>> {
        val response = mapOf(
            "message" to "인증된 사용자입니다",
            "userId" to userPrincipal.id,
            "email" to userPrincipal.email,
            "username" to userPrincipal.userName,
            "role" to userPrincipal.role.name,
            "provider" to userPrincipal.provider.name
        )
        return BaseResponse.onSuccess(response)
    }

    @Operation(
        summary = "USER 권한 필요 API",
        description = "USER 권한을 가진 사용자만 접근 가능한 API",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    fun userEndpoint(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): BaseResponse<String> {
        return BaseResponse.onSuccess("USER 권한을 가진 ${userPrincipal.email}님 환영합니다")
    }

    @Operation(
        summary = "ADMIN 권한 필요 API",
        description = "ADMIN 권한을 가진 사용자만 접근 가능한 API",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    fun adminEndpoint(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): BaseResponse<String> {
        return BaseResponse.onSuccess("ADMIN 권한을 가진 ${userPrincipal.email}님 환영합니다")
    }

    @Operation(
        summary = "복합 권한 API",
        description = "USER 또는 ADMIN 권한을 가진 사용자만 접근 가능한 API",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user-or-admin")
    fun userOrAdminEndpoint(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): BaseResponse<String> {
        return BaseResponse.onSuccess("${userPrincipal.role} 권한을 가진 ${userPrincipal.email}님 환영합니다")
    }
}