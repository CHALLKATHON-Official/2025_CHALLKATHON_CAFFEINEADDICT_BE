package com.challkathon.momento.auth.controller

import com.challkathon.momento.auth.dto.request.SignInRequest
import com.challkathon.momento.auth.dto.request.SignUpRequest
import com.challkathon.momento.auth.dto.response.SignInResponse
import com.challkathon.momento.auth.dto.response.UserInfoResponse
import com.challkathon.momento.auth.service.AuthService
import com.challkathon.momento.auth.util.TokenCookieUtil
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 갱신 등 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthV1Controller(
    private val authService: AuthService,
    private val tokenCookieUtil: TokenCookieUtil
) {

    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입을 진행합니다")
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody signUpRequest: SignUpRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<SignInResponse>> {
        log.info { "회원가입 요청: ${signUpRequest.email}" }
        
        val authResponse = authService.signUp(signUpRequest)
        
        // 토큰을 헤더와 쿠키에 설정
        tokenCookieUtil.setTokens(
            response, 
            authResponse.accessToken, 
            authResponse.refreshToken
        )
        
        val signInResponse = SignInResponse(authResponse.userInfo)
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.onSuccessCreate(signInResponse))
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 진행합니다")
    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody signInRequest: SignInRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<SignInResponse>> {
        log.info { "로그인 요청: ${signInRequest.email}" }
        
        val authResponse = authService.signIn(signInRequest)
        
        // 토큰을 헤더와 쿠키에 설정
        tokenCookieUtil.setTokens(
            response, 
            authResponse.accessToken, 
            authResponse.refreshToken
        )
        
        val signInResponse = SignInResponse(authResponse.userInfo)
        
        return ResponseEntity.ok(BaseResponse.onSuccess(signInResponse))
    }
}