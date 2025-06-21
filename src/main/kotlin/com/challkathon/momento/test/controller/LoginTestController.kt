package com.challkathon.momento.test.controller

import com.challkathon.momento.auth.security.UserPrincipal
import mu.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

private val log = KotlinLogging.logger {}

@Controller
class LoginTestController {

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    fun loginPage(
        @RequestParam(required = false) error: String?,
        model: Model
    ): String {
        if (error != null) {
            model.addAttribute("error", "로그인에 실패했습니다.")
        }
        return "login"
    }

    /**
     * 메인 페이지 (로그인 후)
     */
    @GetMapping("/")
    fun home(
        @AuthenticationPrincipal userPrincipal: UserPrincipal?,
        model: Model
    ): String {
        if (userPrincipal != null) {
            model.addAttribute("username", userPrincipal.userName)
            model.addAttribute("email", userPrincipal.email)
            model.addAttribute("provider", userPrincipal.provider)
            model.addAttribute("isAuthenticated", true)
        } else {
            model.addAttribute("isAuthenticated", false)
        }
        return "home"
    }

    /**
     * OAuth2 로그인 성공 후 리다이렉트 페이지
     */
    @GetMapping("/oauth2/redirect")
    fun oauth2Redirect(
        @AuthenticationPrincipal principal: Any?,
        @RequestParam(required = false) token: String?,
        model: Model
    ): String {
        log.info { "OAuth2 리다이렉트 페이지: principal=$principal, token=${token?.take(20)}..." }
        
        // OAuth2 인증 후 UserPrincipal 또는 OAuth2AuthenticationToken을 처리
        when (principal) {
            is UserPrincipal -> {
                model.addAttribute("username", principal.userName)
                model.addAttribute("email", principal.email)
                model.addAttribute("provider", principal.provider)
            }
            else -> {
                // 토큰이 있다면 토큰 정보만 표시
                if (token != null) {
                    model.addAttribute("username", "인증된 사용자")
                    model.addAttribute("email", "")
                    model.addAttribute("provider", "KAKAO")
                } else {
                    log.warn { "OAuth2 리다이렉트 - 인증 정보 없음" }
                    return "redirect:/login?error=oauth2"
                }
            }
        }
        
        if (token != null) {
            model.addAttribute("accessToken", token)
        }
        
        return "oauth2-success"
    }

    /**
     * 프로필 페이지
     */
    @GetMapping("/profile")
    fun profile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        model: Model
    ): String {
        model.addAttribute("user", userPrincipal)
        return "profile"
    }
}