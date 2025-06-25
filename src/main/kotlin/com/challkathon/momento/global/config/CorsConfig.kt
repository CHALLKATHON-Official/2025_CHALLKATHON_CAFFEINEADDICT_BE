package com.challkathon.momento.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val corsConfiguration = CorsConfiguration().apply {
            // 명시적으로 허용할 오리진 설정
            allowedOrigins = listOf(
                "http://localhost:3000",
                "https://localhost:3000", 
                "http://127.0.0.1:3000",
                "https://momento.vercel.app"
            )
            
            // 모든 HTTP 메서드 허용
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
            
            // 모든 헤더 허용
            allowedHeaders = listOf("*")
            
            // 인증 정보 포함 허용
            allowCredentials = true
            
            // 노출할 헤더 설정
            exposedHeaders = listOf("Authorization", "Content-Type", "Set-Cookie")
            
            // 프리플라이트 요청 캐시 시간 (1시간)
            maxAge = 3600L
        }
        
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfiguration)
        }
        
        return CorsFilter(source)
    }
}