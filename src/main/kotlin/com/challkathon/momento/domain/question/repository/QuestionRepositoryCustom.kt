package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.Question

interface QuestionRepositoryCustom {
    fun findRecentlyUsedByFamily(familyId: Long, days: Int): List<Question>
}