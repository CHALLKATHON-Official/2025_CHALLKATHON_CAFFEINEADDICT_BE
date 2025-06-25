package com.challkathon.momento.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: List<String>,
    private val multipartJackson2HttpMessageConverter: MultipartJackson2HttpMessageConverter
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(*allowedOrigins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
            )
            .exposedHeaders("Authorization", "Set-Cookie", "Content-Type")
            .allowCredentials(true)
            .maxAge(3600)
    }
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // 메시지 컨버터 리스트에 가장 먼저 추가
        converters.add(0, multipartJackson2HttpMessageConverter)
    }
}