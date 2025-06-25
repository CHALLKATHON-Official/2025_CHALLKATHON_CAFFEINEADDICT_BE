package com.challkathon.momento.domain.story.repository

import com.challkathon.momento.domain.story.Story
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface StoryRepository : JpaRepository<Story, Long> {
    fun findAllByExpiredAtBefore(now: LocalDateTime): List<Story>
}
