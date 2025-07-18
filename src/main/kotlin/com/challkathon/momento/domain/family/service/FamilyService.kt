package com.challkathon.momento.domain.family.service

import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.domain.family.dto.response.FamilyCodeResponse
import com.challkathon.momento.domain.family.dto.response.FamilyMemberResponse
import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class FamilyService(
    private val familyRepository: FamilyRepository,
    private val userRepository: UserRepository,
    private val storyRepository: com.challkathon.momento.domain.story.repository.StoryRepository
) {

    @Transactional
    fun createFamily(userId: Long): FamilyCodeResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        if (user.family != null) {
            throw FamilyException(FamilyErrorStatus.FAMILY_ALREADY_JOINED)
        }

        val inviteCode = generateUniqueCode()
        val newFamily = familyRepository.save(
            Family(inviteCode = inviteCode, count = 1)
        )

        user.assignFamily(newFamily)

        return FamilyCodeResponse(inviteCode)
    }

    @Transactional
    fun joinOrMoveFamily(userId: Long, inviteCode: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        val targetFamily = familyRepository.findByInviteCode(inviteCode)
            ?: throw FamilyException(FamilyErrorStatus.INVITE_CODE_NOT_FOUND)

        val currentFamily = user.family

        if (currentFamily != null) {
            if (currentFamily.id == targetFamily.id) {
                throw FamilyException(FamilyErrorStatus.ALREADY_IN_THIS_FAMILY)
            }
            val memberCount = userRepository.countByFamilyIdAndIsActiveTrue(currentFamily.id)
            if (memberCount == 1) {
                familyRepository.delete(currentFamily)
            } else {
                currentFamily.decrementCount()
            }
        }

        targetFamily.incrementCount()
        user.assignFamily(targetFamily)
    }

    private fun generateUniqueCode(): String {
        while (true) {
            val code = (100000..999999).random().toString()
            if (familyRepository.findByInviteCode(code) == null) {
                return code
            }
        }
    }

    @Transactional
    fun getFamilyCode(userId: Long): FamilyCodeResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)

        return FamilyCodeResponse(family.inviteCode)
    }

    fun getFamilyMembers(userId: Long): List<FamilyMemberResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)

        val familyMembers = userRepository.findByFamilyIdAndIsActiveTrue(family.id)

        return familyMembers.map { member ->
            val latestStory = storyRepository.findFirstByUserIdOrderByCreatedAtDesc(member.id)
            FamilyMemberResponse(
                userId = member.id,
                name = member.username,
                familyRole = member.familyRole,
                profileImageUrl = member.profileImageUrl,
                storyId = latestStory?.id
            )
        }
    }
}
