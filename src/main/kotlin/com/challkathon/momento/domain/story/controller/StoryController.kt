package com.challkathon.momento.domain.story.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.story.dto.response.StoryResponse
import com.challkathon.momento.domain.story.service.StoryService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/stories")
@Tag(name = "Story", description = "스토리 관련 API")
class StoryController(
    private val storyService: StoryService
) {

    @PostMapping
    @Operation(
        summary = "스토리 업로드",
        description = "이미지를 업로드하여 스토리를 생성합니다. 24시간 후 자동 만료됩니다."
    )
    fun uploadStory(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestPart image: MultipartFile
    ): ResponseEntity<BaseResponse<String>> {
        storyService.createStory(user.id, image)
        return ResponseEntity.ok(BaseResponse.onSuccessCreate("스토리가 업로드되었습니다."))
    }


    @GetMapping("/{storyId}")
    @Operation(
        summary = "스토리 조회",
        description = "스토리 ID를 통해 이미지를 조회합니다."
    )
    fun getStory(
        @PathVariable storyId: Long
    ): ResponseEntity<BaseResponse<StoryResponse>> {
        val response = storyService.getStory(storyId)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

}
