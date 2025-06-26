package com.challkathon.momento.domain.message.service

import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.domain.message.dto.response.MessageResponse
import com.challkathon.momento.domain.message.entity.Message
import com.challkathon.momento.domain.message.exception.MessageException
import com.challkathon.momento.domain.message.exception.code.MessageErrorStatus
import com.challkathon.momento.domain.message.repository.MessageRepository
import com.challkathon.momento.domain.user.repository.UserRepository
import com.challkathon.momento.global.infrastructure.AmazonS3Manager
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val amazonS3Manager: AmazonS3Manager
) {

    fun createMessage(userId: Long, content: String, reservedDate: LocalDate, image: MultipartFile) {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        val imageUrl = amazonS3Manager.uploadFile(image)

        val message = Message(
            user = user,
            content = content,
            imageUrl = imageUrl,
            reservedAt = reservedDate.atStartOfDay()
        )
        messageRepository.save(message)
    }


    fun getMessageList(userId: Long, date: LocalDate): List<MessageResponse> {
        val messageList = messageRepository.findByUserIdAndDate(userId, date)

        if (messageList.isEmpty()) {
            throw MessageException(MessageErrorStatus._MESSAGE_NOT_FOUND)
        }

        return messageList.map {
            MessageResponse(
                id = it.id,
                content = it.content,
                imageUrl = it.imageUrl
            )
        }
    }

    fun getMessageDetail(userId: Long, messageId: Long): MessageResponse {
        val message = messageRepository.findById(messageId)
            .orElseThrow { MessageException(MessageErrorStatus._MESSAGE_NOT_FOUND) }

        return MessageResponse(
            id = message.id,
            content = message.content,
            imageUrl = message.imageUrl
        )
    }


}
