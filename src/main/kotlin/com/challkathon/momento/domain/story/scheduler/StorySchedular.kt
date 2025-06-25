package com.challkathon.momento.domain.story.scheduler

import com.challkathon.momento.domain.story.repository.StoryRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
class StoryScheduler(
    private val storyRepository: StoryRepository
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun deleteExpiredStories() {
        val now = LocalDateTime.now()
        val expiredStories = storyRepository.findAllByExpiredAtBefore(now)
        storyRepository.deleteAll(expiredStories)
        log.info("만료된 스토리 ${expiredStories.size}개 삭제 완료")
    }
}
