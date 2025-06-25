package com.challkathon.momento.domain.message.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.message.dto.response.MessageResponse
import com.challkathon.momento.domain.message.service.MessageService
import com.challkathon.momento.global.common.BaseResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/messages")
class MessageController(
    private val messageService: MessageService
) {

    @PostMapping
    fun createMessage(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestPart("content") content: String,
        @RequestPart("reservedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) reservedDate: LocalDate,
        @RequestPart("image") image: MultipartFile
    ): ResponseEntity<BaseResponse<String>> {
        messageService.createMessage(user.id, content, reservedDate, image)
        return ResponseEntity.ok(BaseResponse.onSuccessCreate("예약 메시지 생성 완료"))
    }


    @GetMapping
    fun getMessageList(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<BaseResponse<List<MessageResponse>>> {
        val messages = messageService.getMessageList(user.id, date)
        return ResponseEntity.ok(BaseResponse.onSuccess(messages))
    }

    @GetMapping("/{messageId}")
    fun getMessageDetail(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable messageId: Long
    ): ResponseEntity<BaseResponse<MessageResponse>> {
        val message = messageService.getMessageDetail(user.id, messageId)
        return ResponseEntity.ok(BaseResponse.onSuccess(message))
    }

}