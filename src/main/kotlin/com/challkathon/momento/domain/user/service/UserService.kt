package com.challkathon.momento.domain.user.service

import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.domain.user.dto.response.FamilyMemberDto
import com.challkathon.momento.global.infrastructure.AmazonS3Manager
import com.challkathon.momento.domain.user.dto.response.MyPageResponse
import com.challkathon.momento.domain.user.exception.UserException
import com.challkathon.momento.domain.user.exception.code.UserErrorStatus
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
    private val amazonS3Manager: AmazonS3Manager
) {

    @Transactional
    fun updateUserName(userId: Long, name: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        user.updateName(name)
    }

    @Transactional
    fun updateProfileImage(userId: Long, image: MultipartFile) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        val imageUrl = amazonS3Manager.uploadFile(image)
        user.updateProfileImage(imageUrl)
    }

    fun getMyPageInfo(userId: Long): MyPageResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }

        val roleName = user.familyRole?.name ?: "미설정"

        val inviteCode = user.family?.inviteCode ?: "미가입"
        val familyMembers = user.family?.users?.map { member ->
            FamilyMemberDto(
                name = member.username,
                role = member.familyRole?.name ?: "미설정",
                profileImageUrl = member.profileImageUrl
            )
        } ?: emptyList()

        return MyPageResponse(
            name = user.username,
            role = roleName,
            profileImageUrl = user.profileImageUrl,
            inviteCode = inviteCode,
            familyMembers = familyMembers
        )
    }



}