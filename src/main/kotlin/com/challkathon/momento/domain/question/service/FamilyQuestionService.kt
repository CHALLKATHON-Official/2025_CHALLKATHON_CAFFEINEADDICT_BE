package com.challkathon.momento.domain.question.service

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.dto.response.FamilyMemberAnswer
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionAnswersResponse
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionListResponse
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionResponse
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionSummary
import com.challkathon.momento.domain.question.dto.response.QuestionDetail
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.question.exception.QuestionException
import com.challkathon.momento.domain.question.exception.code.QuestionErrorStatus
import com.challkathon.momento.domain.question.repository.FamilyQuestionRepository
import com.challkathon.momento.domain.user.exception.UserException
import com.challkathon.momento.domain.user.exception.code.UserErrorStatus
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
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_DID_NOT_SET)

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
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

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
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        val familyQuestion = familyQuestionRepository.findById(familyQuestionId)
            .orElseThrow { QuestionException(QuestionErrorStatus._FAMILY_QUESTION_NOT_FOUND) }

        // 권한 확인
        if (familyQuestion.family.id != user.family?.id!!) {
            throw QuestionException(QuestionErrorStatus._QUESTION_INVALID_ACCESS)
        }

        // 이미 답변이 있는 경우 재생성 불가
        if (familyQuestion.answers.isNotEmpty()) {
            throw QuestionException(QuestionErrorStatus._QUESTION_ALREADY_ANSWERED)
        }

        // 새 질문 생성
        val newQuestions = questionGenerationManager.getQuestionsForFamily(
            family = familyQuestion.family,
            forceNew = true
        )

        if (newQuestions.isEmpty()) {
            throw QuestionException(QuestionErrorStatus._QUESTION_GENERATION_FAILED)
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

    /**
     * 가족에게 할당된 모든 질문 목록 조회
     */
    fun getAllFamilyQuestions(userId: Long): FamilyQuestionListResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_DID_NOT_SET)

        // 가족에게 할당된 모든 질문을 최신순으로 조회
        val familyQuestions = familyQuestionRepository.findByFamilyIdOrderByAssignedAtDesc(family.id!!)

        val familyMemberCount = family.users.size

        val questionSummaries = familyQuestions.mapIndexed { index, familyQuestion ->
            val answerCount = familyQuestion.answers.size
            val allFamilyAnswered = answerCount >= familyMemberCount

            FamilyQuestionSummary(
                index = index + 1,  // 1부터 시작하는 인덱스
                familyQuestionId = familyQuestion.id!!,
                content = familyQuestion.question.content,
                category = familyQuestion.question.category,
                assignedDate = familyQuestion.assignedAt.toLocalDate(),
                allFamilyAnswered = allFamilyAnswered
            )
        }

        return FamilyQuestionListResponse(questions = questionSummaries)
    }

    /**
     * 특정 질문에 대한 가족 구성원들의 답변 조회
     */
    fun getFamilyQuestionAnswers(userId: Long, familyQuestionId: Long): FamilyQuestionAnswersResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_DID_NOT_SET)

        val familyQuestion = familyQuestionRepository.findById(familyQuestionId)
            .orElseThrow { QuestionException(QuestionErrorStatus._FAMILY_QUESTION_NOT_FOUND) }

        // 권한 확인: 같은 가족 구성원만 조회 가능
        if (familyQuestion.family.id != family.id) {
            throw QuestionException(QuestionErrorStatus._QUESTION_INVALID_ACCESS)
        }

        val questionDetail = QuestionDetail(
            familyQuestionId = familyQuestion.id!!,
            content = familyQuestion.question.content,
            category = familyQuestion.question.category,
            assignedDate = familyQuestion.assignedAt.toLocalDate()
        )

        // 모든 가족 구성원 정보 가져오기
        val familyMembers = family.users

        // 답변한 사용자들의 정보를 Map으로 구성
        val answersMap = familyQuestion.answers.associateBy { it.user.id }

        val memberAnswers = familyMembers.map { member ->
            val answer = answersMap[member.id]

            FamilyMemberAnswer(
                answerId = answer?.id,
                memberName = member.username,
                memberProfileImage = member.profileImageUrl,
                content = answer?.content,
                answeredAt = answer?.createdAt
            )
        }

        return FamilyQuestionAnswersResponse(
            question = questionDetail,
            answers = memberAnswers
        )
    }
}
