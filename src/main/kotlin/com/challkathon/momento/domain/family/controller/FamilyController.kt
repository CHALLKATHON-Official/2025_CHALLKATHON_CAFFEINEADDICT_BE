package com.challkathon.momento.domain.family.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.family.dto.response.FamilyCodeResponse
import com.challkathon.momento.domain.family.dto.response.FamilyMemberResponse
import com.challkathon.momento.domain.family.service.FamilyService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/family")
@Tag(name = "Family", description = "가족 방 생성 및 초대 관련 API")
class FamilyController(
    private val familyService: FamilyService
) {

    @PostMapping("/create")
    @Operation(
        summary = "가족 방 생성",
        description = "유저가 새로운 가족 방을 생성하고 초대 코드를 반환합니다."
    )
    fun createFamily(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<FamilyCodeResponse>> {
        val response = familyService.createFamily(userPrincipal.id)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

    @PostMapping("/join")
    @Operation(
        summary = "가족 방 참여 또는 변경",
        description = "초대 코드를 통해 기존 가족방을 떠나고 새로운 가족방에 참여합니다."
    )
    fun joinOrMoveFamily(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "초대 코드", required = true)
        @RequestParam inviteCode: String
    ): ResponseEntity<BaseResponse<String>> {
        familyService.joinOrMoveFamily(userPrincipal.id, inviteCode)
        return ResponseEntity.ok(BaseResponse.onSuccess("가족 방 참여가 완료되었습니다."))
    }


    @GetMapping("/code")
    @Operation(
        summary = "현재 가족의 초대 코드 조회",
        description = "현재 속한 가족의 초대 코드를 조회합니다."
    )
    fun getFamilyCode(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<FamilyCodeResponse>> {
        val response = familyService.getFamilyCode(userPrincipal.id)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

    @GetMapping("/members")
    @Operation(
        summary = "가족 구성원 목록 조회",
        description = "현재 속한 가족의 모든 구성원 정보를 조회합니다. profileImageUrl이 없으면 null을 반환합니다."
    )
    fun getFamilyMembers(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<List<FamilyMemberResponse>>> {
        val response = familyService.getFamilyMembers(userPrincipal.id)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }
}
