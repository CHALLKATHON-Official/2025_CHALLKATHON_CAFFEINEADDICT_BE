package com.challkathon.momento.domain.family.service

import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.domain.family.dto.response.FamilyCodeResponse
import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.exception.FamilyAlreadyJoinedException
import com.challkathon.momento.domain.family.exception.FamilyInviteCodeNotFoundException
import com.challkathon.momento.domain.family.exception.FamilyNotJoinedException
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class FamilyService(
    private val familyRepository: FamilyRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createFamily(userId: Long): FamilyCodeResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        if (user.family != null) {
            throw FamilyAlreadyJoinedException()
        }

        val inviteCode = generateUniqueCode()
        val newFamily = familyRepository.save(
            Family(inviteCode = inviteCode, count = 1)
        )

        user.assignFamily(newFamily)

        return FamilyCodeResponse(inviteCode)
    }

    @Transactional
    fun joinFamily(userId: Long, inviteCode: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        if (user.family != null) {
            throw FamilyAlreadyJoinedException()
        }

        val targetFamily = familyRepository.findByInviteCode(inviteCode)
            ?: throw FamilyInviteCodeNotFoundException()

        user.assignFamily(targetFamily)
        targetFamily.incrementCount()
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
            ?: throw FamilyNotJoinedException()

        return FamilyCodeResponse(family.inviteCode)
    }
}
