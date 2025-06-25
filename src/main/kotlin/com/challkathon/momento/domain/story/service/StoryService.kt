package com.challkathon.momento.domain.story.service

import com.challkathon.momento.auth.exception.UserNotFoundException
import com.challkathon.momento.domain.user.repository.UserRepository
import com.challkathon.momento.domain.s3.service.AmazonS3Manager
import com.challkathon.momento.domain.story.Story
import com.challkathon.momento.domain.story.dto.response.StoryResponse
import com.challkathon.momento.domain.story.exception.StoryNotFoundException
import com.challkathon.momento.domain.story.repository.StoryRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class StoryService(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository,
    private val amazonS3Manager: AmazonS3Manager
) {

    @Transactional
    fun createStory(userId: Long, image: MultipartFile) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }

        val imageUrl = amazonS3Manager.uploadFile(image)
        val expiredAt = LocalDateTime.now().plusHours(24)

        val story = Story(
            user = user,
            imageUrl = imageUrl,
            expiredAt = expiredAt
        )
        storyRepository.save(story)
    }

    fun getStory(storyId: Long): StoryResponse {
        val story = storyRepository.findById(storyId)
            .orElseThrow { StoryNotFoundException() }

        return StoryResponse(imageUrl = story.imageUrl)
    }
}
