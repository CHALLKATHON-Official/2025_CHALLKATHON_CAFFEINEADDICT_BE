package com.challkathon.momento.auth.controller

import com.challkathon.momento.auth.dto.response.UserDetails
import com.challkathon.momento.auth.dto.response.UserInfoResponse
import com.challkathon.momento.auth.exception.UserNotFoundException
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class UserInfoController(
    private val userRepository: UserRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserInfoController::class.java)
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userPrincipal: UserPrincipal): ResponseEntity<UserInfoResponse> {
        try {
            log.info("사용자 정보 조회 요청: 사용자 ID = ${userPrincipal.id}")

            // 최신 사용자 정보 조회 (DB에서)
            val user = userRepository.findById(userPrincipal.id)
                .orElseThrow { UserNotFoundException() }

            val userDetails = UserDetails(
                userId = user.id,
                email = user.email,
                username = user.username,
                hasFamily = user.family != null,
                needsFamilyRole = !user.familyRoleSelected,
                familyId = user.family?.id,
                authProvider = user.authProvider,
                profileImageUrl = user.profileImageUrl,
                lastLoginAt = user.lastLoginAt
            )

            val response = UserInfoResponse(
                success = true,
                user = userDetails
            )

            log.info("사용자 정보 조회 성공: 사용자 ID = ${user.id}, hasFamily = ${user.family != null}")

            return ResponseEntity.ok(response)

        } catch (ex: Exception) {
            log.error("사용자 정보 조회 실패: ${ex.message}", ex)
            throw ex
        }
    }
}