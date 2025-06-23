package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.dto.request.SignInRequest
import com.challkathon.momento.auth.dto.request.SignUpRequest
import com.challkathon.momento.auth.dto.response.AuthResponse
import com.challkathon.momento.auth.dto.response.UserInfoResponse
import com.challkathon.momento.auth.provider.JwtProvider
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val customUserDetailsService: CustomUserDetailsService
) {

    fun signUp(request: SignUpRequest): AuthResponse {
        // 회원가입 로직
        val user = User.createLocalUser(
            email = request.email,
            username = request.username,
            encodedPassword = passwordEncoder.encode(request.password),
            familyRole = request.familyRole

        )
        
        val savedUser = userRepository.save(user)
        val userDetails = customUserDetailsService.loadUserByUsername(savedUser.email)
        
        val accessToken = jwtProvider.generateAccessToken(userDetails)
        val refreshToken = jwtProvider.generateRefreshToken(savedUser.email, savedUser.refreshTokenVersion)
        
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userInfo = UserInfoResponse(
                id = savedUser.id,
                email = savedUser.email,
                username = savedUser.username,
                role = savedUser.role,
                provider = savedUser.authProvider
            )
        )
    }

    fun signIn(request: SignInRequest): AuthResponse {
        // 로그인 로직
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다") }
        
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw RuntimeException("비밀번호가 일치하지 않습니다")
        }
        
        val userDetails = customUserDetailsService.loadUserByUsername(user.email)
        val accessToken = jwtProvider.generateAccessToken(userDetails)
        val refreshToken = jwtProvider.generateRefreshToken(user.email, user.refreshTokenVersion)
        
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userInfo = UserInfoResponse(
                id = user.id,
                email = user.email,
                username = user.username,
                role = user.role,
                provider = user.authProvider
            )
        )
    }
}