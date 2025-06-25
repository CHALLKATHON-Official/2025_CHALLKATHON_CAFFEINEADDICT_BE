package com.challkathon.momento.domain.user.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.user.dto.response.MyPageResponse
import com.challkathon.momento.domain.user.service.UserService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @PatchMapping("/me/name")
    @Operation(summary = "사용자 이름 변경", description = "현재 로그인된 사용자의 이름을 수정합니다.")
    fun updateName(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestParam name: String
    ): ResponseEntity<BaseResponse<String>> {
        userService.updateUserName(user.id, name)
        return ResponseEntity.ok(BaseResponse.onSuccess("사용자 이름 변경 완료"))
    }

    @PatchMapping("/me/image")
    @Operation(summary = "사용자 프로필 이미지 변경", description = "현재 로그인된 사용자의 이미지를 수정합니다.")
    fun updateProfileImage(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestPart image: MultipartFile
    ): ResponseEntity<BaseResponse<String>> {
        userService.updateProfileImage(user.id, image)
        return ResponseEntity.ok(BaseResponse.onSuccess("사용자 프로필 이미지 변경 완료"))
    }

    @GetMapping("/me")
    fun getMyPageInfo(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<BaseResponse<MyPageResponse>> {
        val response = userService.getMyPageInfo(user.id)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

}
