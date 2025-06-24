package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.QQuestion.question
import com.challkathon.momento.domain.question.entity.mapping.QFamilyQuestion.familyQuestion
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class QuestionRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : QuestionRepositoryCustom {

    override fun findRecentlyUsedByFamily(familyId: Long, days: Int): List<Question> {
        return queryFactory
            .selectDistinct(question)
            .from(question)
            .join(familyQuestion).on(familyQuestion.question.eq(question))
            .where(
                familyQuestion.family.id.eq(familyId),
                familyQuestion.assignedAt.goe(LocalDateTime.now().minusDays(days.toLong()))
            )
            .fetch()
    }
}