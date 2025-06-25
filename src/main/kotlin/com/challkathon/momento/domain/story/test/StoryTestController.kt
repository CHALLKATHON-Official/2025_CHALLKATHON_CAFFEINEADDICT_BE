package com.challkathon.momento.domain.story.test

import com.challkathon.momento.domain.story.dto.response.StoryResponse
import com.challkathon.momento.domain.story.service.StoryService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/test/v1/stories")
@Tag(name = "Story Test", description = "스토리 관련 테스트 API")
class StoryTestController(
    private val storyService: StoryService
) {

    @PostMapping
    @Operation(
        summary = "스토리 업로드 (테스트용)",
        description = "임의의 사용자 ID를 사용하여 이미지를 업로드합니다."
    )
    fun uploadStoryTest(
        @RequestPart image: MultipartFile
    ): ResponseEntity<BaseResponse<String>> {
        val testUserId = 1L
        storyService.createStory(testUserId, image)
        return ResponseEntity.ok(BaseResponse.onSuccessCreate("스토리가 업로드되었습니다."))
    }

    @GetMapping("/{storyId}")
    @Operation(
        summary = "스토리 조회 (테스트용)",
        description = "스토리 ID를 통해 인증 없이 이미지를 조회합니다."
    )
    fun getStoryTest(
        @PathVariable storyId: Long
    ): ResponseEntity<BaseResponse<StoryResponse>> {
        val response = storyService.getStory(storyId)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }
}
