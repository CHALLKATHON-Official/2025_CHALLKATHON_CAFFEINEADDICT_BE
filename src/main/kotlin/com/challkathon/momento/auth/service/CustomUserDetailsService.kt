package com.challkathon.momento.auth.service

import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.repository.UserRepository
import com.challkathon.momento.auth.security.UserPrincipal
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    /**
     * Spring Security 표준 메서드
     * 모든 인증 과정에서 이 메서드를 통해 UserDetails를 얻음
     * User 엔티티를 UserPrincipal로 변환하여 반환
     */
    @Cacheable("userDetails", key = "#username")
    override fun loadUserByUsername(username: String): UserDetails {
        val user = findUserByEmail(username)
        return UserPrincipal.create(user)
    }

    /**
     * 비즈니스 로직에서 User 엔티티가 필요한 경우
     * 예: 프로필 업데이트, 사용자 정보 수정 등
     */
    @Cacheable("users", key = "#username")
    fun loadUserEntityByUsername(username: String): User {
        return findUserByEmail(username)
    }

    /**
     * ID로 사용자 조회 (JWT 토큰에서 사용자 ID 추출 시 사용)
     */
    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다: $id") }
        return UserPrincipal.create(user)
    }

    /**
     * 사용자 존재 여부 확인 (회원가입 시 중복 검사 등)
     */
    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
    
    /**
     * 사용자 엔티티 저장
     */
    @Transactional
    fun saveUser(user: User): User {
        return userRepository.save(user)
    }

    /**
     * 공통 사용자 조회 로직
     */
    private fun findUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다: $email") }
    }
}