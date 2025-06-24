package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FamilyQuestionRepository : JpaRepository<FamilyQuestion, Long>, FamilyQuestionRepositoryCustom
