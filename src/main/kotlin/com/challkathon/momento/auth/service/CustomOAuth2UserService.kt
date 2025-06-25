package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.dto.KakaoOAuth2UserInfo
import com.challkathon.momento.auth.dto.OAuth2UserInfo
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import com.challkathon.momento.domain.user.entity.enums.Role
import com.challkathon.momento.domain.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        
        return try {
            processOAuth2User(userRequest, oAuth2User)
        } catch (ex: Exception) {
            logger.error(ex) { "OAuth2 user processing failed" }
            throw ex
        }
    }

    private fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        val userInfo = getOAuth2UserInfo(registrationId, oAuth2User.attributes)
        
        if (userInfo.getEmail().isEmpty()) {
            throw IllegalArgumentException("Email not found from OAuth2 provider")
        }

        val user = userRepository.findByEmailAndAuthProvider(userInfo.getEmail(), getAuthProvider(registrationId))
            ?.let { updateExistingUser(it, userInfo) }
            ?: createNewUser(userInfo, registrationId)

        return UserPrincipal.create(user, oAuth2User.attributes)
    }

    private fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw IllegalArgumentException("Sorry! Login with $registrationId is not supported yet.")
        }
    }

    private fun getAuthProvider(registrationId: String): AuthProvider {
        return when (registrationId.lowercase()) {
            "kakao" -> AuthProvider.KAKAO
            else -> throw IllegalArgumentException("Unsupported provider: $registrationId")
        }
    }

    private fun createNewUser(userInfo: OAuth2UserInfo, registrationId: String): User {
        val user = User(
            email = userInfo.getEmail(),
            username = userInfo.getName(),
            providerId = userInfo.getId(),
            profileImageUrl = userInfo.getImageUrl(),
            authProvider = getAuthProvider(registrationId),
            role = Role.USER,
            lastLoginAt = LocalDateTime.now()
        )
        
        return userRepository.save(user)
    }

    private fun updateExistingUser(existingUser: User, userInfo: OAuth2UserInfo): User {
        existingUser.apply {
            username = userInfo.getName()
            profileImageUrl = userInfo.getImageUrl()
            lastLoginAt = LocalDateTime.now()
            lastActiveAt = LocalDateTime.now()
        }
        
        return userRepository.save(existingUser)
    }
}