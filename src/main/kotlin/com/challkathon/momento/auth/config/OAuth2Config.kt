package com.challkathon.momento.auth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import mu.KotlinLogging

@Configuration
class OAuth2Config(
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id:\${KAKAO_CLIENT_ID}}")
    private val kakaoClientId: String,
    
    @Value("\${spring.security.oauth2.client.registration.kakao.client-secret:\${KAKAO_CLIENT_SECRET}}")
    private val kakaoClientSecret: String,
    
    private val environment: Environment
) {
    
    private val logger = KotlinLogging.logger {}

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(kakaoClientRegistration())
    }

    private fun kakaoClientRegistration(): ClientRegistration {
        val redirectUri = determineRedirectUri()
        
        logger.info { "OAuth2 설정 - 활성 프로필: ${environment.activeProfiles.contentToString()}" }
        logger.info { "OAuth2 설정 - Redirect URI: $redirectUri" }
        
        return ClientRegistration.withRegistrationId("kakao")
            .clientId(kakaoClientId)
            .clientSecret(kakaoClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(redirectUri)
            .scope("profile_nickname", "account_email")
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .userNameAttributeName("id")
            .clientName("kakao")
            .build()
    }
    
    private fun determineRedirectUri(): String {
        val activeProfiles = environment.activeProfiles
        
        return when {
            // 로컬 환경 감지 (프로필이 local이거나 활성 프로필이 없는 경우)
            activeProfiles.contains("local") || activeProfiles.isEmpty() -> {
                "http://localhost:8080/login/oauth2/code/kakao"
            }
            // 개발/운영 환경
            else -> {
                "https://dev.caffeineoverdose.shop/login/oauth2/code/kakao"
            }
        }
    }
}