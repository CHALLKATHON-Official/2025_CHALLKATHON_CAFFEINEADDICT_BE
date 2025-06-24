package com.challkathon.momento.auth.dto

import com.challkathon.momento.auth.exception.OAuth2AuthenticationException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.domain.user.entity.enums.AuthProvider

/**
 * OAuth2 사용자 정보 DTO
 */
data class OAuth2UserInfo(
    val id: String,
    val email: String,
    val name: String,
    val imageUrl: String?,
    val provider: AuthProvider,
    val attributes: Map<String, Any>
) {
    companion object {
        fun of(provider: AuthProvider, attributes: Map<String, Any>): OAuth2UserInfo {
            return when (provider) {
                AuthProvider.GOOGLE -> extractGoogleUserInfo(attributes)
                AuthProvider.GITHUB -> extractGithubUserInfo(attributes)
                AuthProvider.KAKAO -> extractKakaoUserInfo(attributes)
                AuthProvider.NAVER -> extractNaverUserInfo(attributes)
                else -> throw OAuth2AuthenticationException(AuthErrorStatus._OAUTH2_PROVIDER_NOT_SUPPORTED)
            }
        }
        
        private fun extractGoogleUserInfo(attributes: Map<String, Any>): OAuth2UserInfo {
            return OAuth2UserInfo(
                id = attributes["sub"] as String,
                email = attributes["email"] as String,
                name = attributes["name"] as String,
                imageUrl = attributes["picture"] as? String,
                provider = AuthProvider.GOOGLE,
                attributes = attributes
            )
        }
        
        private fun extractGithubUserInfo(attributes: Map<String, Any>): OAuth2UserInfo {
            return OAuth2UserInfo(
                id = attributes["id"].toString(),
                email = attributes["email"] as? String ?: "${attributes["login"]}@github.local",
                name = attributes["name"] as? String ?: attributes["login"] as String,
                imageUrl = attributes["avatar_url"] as? String,
                provider = AuthProvider.GITHUB,
                attributes = attributes
            )
        }
        
        private fun extractKakaoUserInfo(attributes: Map<String, Any>): OAuth2UserInfo {
            val kakaoAccount = attributes["kakao_account"] as Map<*, *>
            val profile = kakaoAccount["profile"] as Map<*, *>
            
            return OAuth2UserInfo(
                id = attributes["id"].toString(),
                email = kakaoAccount["email"] as? String ?: "${attributes["id"]}@kakao.local",
                name = profile["nickname"] as String,
                imageUrl = profile["profile_image_url"] as? String,
                provider = AuthProvider.KAKAO,
                attributes = attributes
            )
        }
        
        private fun extractNaverUserInfo(attributes: Map<String, Any>): OAuth2UserInfo {
            val response = attributes["response"] as Map<*, *>
            
            return OAuth2UserInfo(
                id = response["id"] as String,
                email = response["email"] as String,
                name = response["name"] as String,
                imageUrl = response["profile_image"] as? String,
                provider = AuthProvider.NAVER,
                attributes = attributes
            )
        }
    }
}