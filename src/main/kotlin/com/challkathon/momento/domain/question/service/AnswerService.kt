package com.challkathon.momento.domain.question.service

import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.domain.question.dto.request.CreateAnswerRequest
import com.challkathon.momento.domain.question.dto.response.AnswerListResponse
import com.challkathon.momento.domain.question.dto.response.AnswerResponse
import com.challkathon.momento.domain.question.entity.Answer
import com.challkathon.momento.domain.question.exception.QuestionException
import com.challkathon.momento.domain.question.exception.code.QuestionErrorStatus
import com.challkathon.momento.domain.question.repository.AnswerRepository
import com.challkathon.momento.domain.question.repository.FamilyQuestionRepository
import com.challkathon.momento.domain.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AnswerService(
    private val answerRepository: AnswerRepository,
    private val familyQuestionRepository: FamilyQuestionRepository,
    private val userRepository: UserRepository
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 답변 생성
     */
    fun createAnswer(familyQuestionId: Long, userId: Long, request: CreateAnswerRequest): AnswerResponse {
        // 1. FamilyQuestion 조회
        val familyQuestion = familyQuestionRepository.findById(familyQuestionId)
            .orElseThrow { QuestionException(QuestionErrorStatus._FAMILY_QUESTION_NOT_FOUND) }

        // 2. 사용자 조회
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        // 3. 가족 소속 검증
        if (user.family?.id != familyQuestion.family.id) {
            throw FamilyException(FamilyErrorStatus.FAMILY_ACCESS_DENIED)
        }

        // 4. 중복 답변 체크
        val existingAnswer = answerRepository.findByFamilyQuestionAndUser(familyQuestion, user)
        if (existingAnswer != null) {
            throw QuestionException(QuestionErrorStatus._ANSWER_ALREADY_EXISTS)
        }

        // 5. 답변 생성
        val answer = Answer(
            familyQuestion = familyQuestion,
            user = user,
            content = request.content
        )

        val savedAnswer = answerRepository.save(answer)

        // 6. FamilyQuestion 상태 업데이트
        familyQuestion.updateStatus()
        familyQuestionRepository.save(familyQuestion)

        logger.info { "Answer created for familyQuestion: $familyQuestionId by user: $userId" }

        return mapToAnswerResponse(savedAnswer)
    }

    /**
     * 특정 FamilyQuestion의 모든 답변 조회
     */
    @Transactional(readOnly = true)
    fun getAnswersByFamilyQuestion(familyQuestionId: Long, userId: Long): AnswerListResponse {
        // 1. FamilyQuestion 조회
        val familyQuestion = familyQuestionRepository.findById(familyQuestionId)
            .orElseThrow { QuestionException(QuestionErrorStatus._FAMILY_QUESTION_NOT_FOUND) }

        // 2. 사용자 조회 및 가족 소속 검증
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        if (user.family?.id != familyQuestion.family.id) {
            throw FamilyException(FamilyErrorStatus.FAMILY_ACCESS_DENIED)
        }

        // 3. 답변 목록 조회
        val answers = answerRepository.findByFamilyQuestionOrderByCreatedAtAsc(familyQuestion)

        return AnswerListResponse(
            familyQuestionId = familyQuestion.id,
            questionContent = familyQuestion.question.content,
            answers = answers.map { mapToAnswerResponse(it) }
        )
    }

    /**
     * 특정 답변 조회
     */
    @Transactional(readOnly = true)
    fun getAnswer(answerId: Long, userId: Long): AnswerResponse {
        // 1. 답변 조회
        val answer = answerRepository.findById(answerId)
            .orElseThrow { QuestionException(QuestionErrorStatus._ANSWER_NOT_FOUND) }

        // 2. 사용자 조회 및 가족 소속 검증
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        if (user.family?.id != answer.familyQuestion.family.id) {
            throw FamilyException(FamilyErrorStatus.FAMILY_ACCESS_DENIED)
        }

        return mapToAnswerResponse(answer)
    }

    private fun mapToAnswerResponse(answer: Answer): AnswerResponse {
        return AnswerResponse(
            answerId = answer.id,
            content = answer.content,
            authorName = answer.user.username,
            authorProfileImage = answer.user.profileImageUrl,
            authorFamilyRole = answer.user.familyRole?.name,
            createdAt = answer.createdAt,
            updatedAt = answer.updatedAt
        )
    }
}