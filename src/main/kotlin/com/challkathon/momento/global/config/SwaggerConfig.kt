package com.challkathon.momento.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Value("\${spring.application.name:momento}")
    private lateinit var applicationName: String

    @Bean
    fun openAPI(): OpenAPI {
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("bearerAuth")
            .description("JWT 토큰을 입력하세요")

        val securityRequirement = SecurityRequirement().addList("bearerAuth")

        return OpenAPI()
            .info(apiInfo())
            .servers(servers())
            .addSecurityItem(securityRequirement)
            .components(
                Components()
                    .addSecuritySchemes("bearerAuth", securityScheme)
            )
    }

    private fun apiInfo(): Info {
        return Info()
            .title("$applicationName API")
            .description("Spring Security + OAuth2 기반 인증 시스템 API 문서")
            .version("1.0.0")
            .contact(
                Contact()
                    .name("개발팀")
                    .email("dev@challkathon.com")
                    .url("https://github.com/challkathon/momento")
            )
            .license(
                License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")
            )
    }

    private fun servers(): List<Server> {
        return listOf(
            Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버"),
            Server()
                .url("https://api.challkathon.com")
                .description("운영 서버")
        )
    }
}