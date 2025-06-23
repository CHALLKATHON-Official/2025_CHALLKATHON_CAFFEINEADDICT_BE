package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import com.challkathon.momento.domain.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        try {
            return processOAuth2User(userRequest, oAuth2User)
        } catch (ex: Exception) {
            throw OAuth2AuthenticationException("OAuth2 사용자 처리 중 오류가 발생했습니다: ${ex.message}")
        }
    }

    private fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        val authProvider = AuthProvider.valueOf(registrationId.uppercase())

        val oAuth2UserInfo = when (authProvider) {
            AuthProvider.KAKAO -> KakaoOAuth2UserInfo(oAuth2User.attributes)
            AuthProvider.GOOGLE -> GoogleOAuth2UserInfo(oAuth2User.attributes)
            else -> throw OAuth2AuthenticationException("지원되지 않는 OAuth2 제공자입니다: $registrationId")
        }

        if (oAuth2UserInfo.getEmail().isNullOrEmpty()) {
            throw OAuth2AuthenticationException("OAuth2 제공자로부터 이메일을 가져올 수 없습니다.")
        }

        val user = userRepository.findByEmailAndAuthProvider(oAuth2UserInfo.getEmail()!!, authProvider)
            .orElse(null) ?: registerNewUser(userRequest, oAuth2UserInfo)

        return UserPrincipal.create(user, oAuth2User.attributes)
    }

    @Transactional
    private fun registerNewUser(userRequest: OAuth2UserRequest, oAuth2UserInfo: OAuth2UserInfo): User {
        val authProvider = AuthProvider.valueOf(userRequest.clientRegistration.registrationId.uppercase())

        // 새 사용자는 family role을 나중에 선택하도록 null로 생성
        val user = User.createOAuth2User(
            email = oAuth2UserInfo.getEmail()!!,
            username = oAuth2UserInfo.getName() ?: "사용자",
            authProvider = authProvider,
            providerId = oAuth2UserInfo.getId(),
            profileImageUrl = oAuth2UserInfo.getImageUrl()
        )

        user.updateLastLogin()

        return userRepository.save(user)
    }
}

// OAuth2 사용자 정보 추상화
abstract class OAuth2UserInfo(val attributes: Map<String, Any>) {
    abstract fun getId(): String
    abstract fun getName(): String?
    abstract fun getEmail(): String?
    abstract fun getImageUrl(): String?
}

// 카카오 OAuth2 사용자 정보
class KakaoOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override fun getId(): String = attributes["id"].toString()

    override fun getName(): String? {
        val properties = attributes["properties"] as? Map<*, *>
        return properties?.get("nickname") as? String
    }

    override fun getEmail(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        return kakaoAccount?.get("email") as? String
    }

    override fun getImageUrl(): String? {
        val properties = attributes["properties"] as? Map<*, *>
        return properties?.get("profile_image") as? String
    }
}

// 구글 OAuth2 사용자 정보
class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override fun getId(): String = attributes["sub"] as String
    override fun getName(): String? = attributes["name"] as? String
    override fun getEmail(): String? = attributes["email"] as? String
    override fun getImageUrl(): String? = attributes["picture"] as? String
}