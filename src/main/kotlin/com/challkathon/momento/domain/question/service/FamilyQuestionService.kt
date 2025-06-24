package com.challkathon.momento.domain.question.service

import com.challkathon.momento.auth.exception.UserNotFoundException
import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionResponse
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.question.repository.FamilyQuestionRepository
import com.challkathon.momento.domain.user.repository.UserRepository
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class FamilyQuestionService(
    private val familyQuestionRepository: FamilyQuestionRepository,
    private val familyRepository: FamilyRepository,
    private val userRepository: UserRepository,
    private val questionGenerationManager: QuestionGenerationManager
) {

    /**
     * 오늘의 가족 질문 조회
     */
    fun getTodayQuestions(userId: String): List<FamilyQuestionResponse> {
        val user = userRepository.findById(userId.toLong())
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        val family = user.family
            ?: throw IllegalStateException("가족이 설정되지 않았습니다")

        return familyQuestionRepository.findTodayQuestionsByFamilyId(family.id!!)
            .map { FamilyQuestionResponse.from(it) }
    }

    /**
     * 기간별 질문 히스토리 조회
     */
    fun getQuestionHistory(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<FamilyQuestionResponse> {
        val user = userRepository.findById(userId.toLong())
            .orElseThrow { UserNotFoundException() }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_DID_NOT_SET)

        return familyQuestionRepository.findByFamilyIdAndDateRange(
            familyId = family.id!!,
            startDate = startDate.atStartOfDay(),
            endDate = endDate.atTime(23, 59, 59)
        ).map { FamilyQuestionResponse.from(it) }
    }

    /**
     * 가족에게 질문 할당
     */
    @Transactional
    fun assignQuestionsToFamily(
        family: Family,
        questions: List<Question>
    ): List<FamilyQuestion> {
        val familyQuestions = questions.map { question ->
            question.incrementUsageCount()

            FamilyQuestion(
                question = question,
                family = family,
                assignedAt = LocalDateTime.now(),
                dueDate = LocalDateTime.now().plusDays(1),
                status = FamilyQuestionStatus.ASSIGNED
            )
        }

        val savedQuestions = familyQuestionRepository.saveAll(familyQuestions)

        log.info { "Assigned ${savedQuestions.size} questions to family ${family.id}" }

        // TODO: 알림 서비스 연동
        // notificationService.sendQuestionNotification(family, savedQuestions)

        return savedQuestions
    }

    /**
     * 질문 재생성
     */
    @Transactional
    fun regenerateQuestion(userId: String, familyQuestionId: Long): FamilyQuestionResponse = runBlocking {
        val user = userRepository.findById(userId.toLong())
            .orElseThrow { UserNotFoundException("사용자를 찾을 수 없습니다: $userId") }

        val familyQuestion = familyQuestionRepository.findById(familyQuestionId)
            .orElseThrow { IllegalArgumentException("질문을 찾을 수 없습니다: $familyQuestionId") }

        // 권한 확인
        if (familyQuestion.family.id != user.family?.id!!) {
            throw IllegalAccessException("해당 질문에 접근할 권한이 없습니다")
        }

        // 이미 답변이 있는 경우 재생성 불가
        if (familyQuestion.answers.isNotEmpty()) {
            throw IllegalStateException("이미 답변이 있는 질문은 재생성할 수 없습니다")
        }

        // 새 질문 생성
        val newQuestions = questionGenerationManager.getQuestionsForFamily(
            family = familyQuestion.family,
            forceNew = true
        )

        if (newQuestions.isEmpty()) {
            throw IllegalStateException("새 질문을 생성할 수 없습니다")
        }

        // 기존 질문을 새 질문으로 교체
        val newFamilyQuestion = FamilyQuestion(
            question = newQuestions.first(),
            family = familyQuestion.family,
            assignedAt = familyQuestion.assignedAt,
            dueDate = familyQuestion.dueDate,
            status = familyQuestion.status
        )

        // 기존 질문 삭제
        familyQuestionRepository.delete(familyQuestion)

        // 새 질문 저장
        val saved = familyQuestionRepository.save(newFamilyQuestion)

        return@runBlocking FamilyQuestionResponse.from(saved)
    }

    /**
     * 만료된 질문 상태 업데이트
     */
    @Transactional
    fun updateExpiredQuestions() {
        val expiredQuestions = familyQuestionRepository.findExpiredQuestions(
            FamilyQuestionStatus.ASSIGNED
        )

        expiredQuestions.forEach { question ->
            question.status = FamilyQuestionStatus.EXPIRED
        }

        familyQuestionRepository.saveAll(expiredQuestions)

        log.info { "Updated ${expiredQuestions.size} expired questions" }
    }
}
