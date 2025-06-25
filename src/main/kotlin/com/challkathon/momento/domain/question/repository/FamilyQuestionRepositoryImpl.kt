package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.question.entity.mapping.QFamilyQuestion.familyQuestion
import com.challkathon.momento.domain.question.entity.QQuestion.question
import com.challkathon.momento.domain.question.entity.QAnswer.answer
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class FamilyQuestionRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : FamilyQuestionRepositoryCustom {

    override fun findTodayQuestionsByFamilyId(familyId: Long): List<FamilyQuestion> {
        val today = LocalDateTime.now().toLocalDate()
        
        return queryFactory
            .selectFrom(familyQuestion)
            .join(familyQuestion.question, question).fetchJoin()
            .where(
                familyQuestion.family.id.eq(familyId),
                familyQuestion.assignedAt.between(
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay()
                )
            )
            .orderBy(familyQuestion.assignedAt.desc())
            .fetch()
    }

    override fun findByFamilyIdAndDateRange(
        familyId: Long, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): List<FamilyQuestion> {
        return queryFactory
            .selectFrom(familyQuestion)
            .join(familyQuestion.question, question).fetchJoin()
            .where(
                familyQuestion.family.id.eq(familyId),
                familyQuestion.assignedAt.between(startDate, endDate)
            )
            .orderBy(familyQuestion.assignedAt.desc())
            .fetch()
    }

    override fun countAnsweredQuestionsSince(familyId: Long, since: LocalDateTime): Long {
        return queryFactory
            .select(familyQuestion.id.countDistinct())
            .from(familyQuestion)
            .join(familyQuestion.answers, answer)
            .where(
                familyQuestion.family.id.eq(familyId),
                familyQuestion.assignedAt.goe(since)
            )
            .fetchOne() ?: 0L
    }

    override fun findExpiredQuestions(status: FamilyQuestionStatus): List<FamilyQuestion> {
        return queryFactory
            .selectFrom(familyQuestion)
            .where(
                familyQuestion.status.eq(status),
                familyQuestion.dueDate.lt(LocalDateTime.now())
            )
            .fetch()
    }

    override fun findByFamilyIdOrderByAssignedAtDesc(familyId: Long): List<FamilyQuestion> {
        return queryFactory
            .selectFrom(familyQuestion)
            .join(familyQuestion.question, question).fetchJoin()
            .leftJoin(familyQuestion.answers, answer).fetchJoin()
            .join(familyQuestion.family).fetchJoin()
            .where(familyQuestion.family.id.eq(familyId))
            .orderBy(familyQuestion.assignedAt.desc())
            .fetch()
    }
}