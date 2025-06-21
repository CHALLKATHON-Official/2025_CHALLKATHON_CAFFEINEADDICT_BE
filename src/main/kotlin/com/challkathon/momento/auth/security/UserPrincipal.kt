package com.challkathon.momento.auth.security

import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import com.challkathon.momento.domain.user.entity.enums.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * Spring Security의 UserDetails 인터페이스를 구현하는 래퍼 클래스
 * User 엔티티를 조합(Composition)으로 사용하여 보안 컨텍스트에서 사용
 * 
 * 주의: 
 * - userName 프로퍼티는 User 엔티티의 username을 노출 (충돌 방지를 위해 다른 이름 사용)
 * - getUsername() 메서드는 UserDetails 인터페이스 구현 (email 반환)
 * - getName() 메서드는 OAuth2User 인터페이스 구현 (user.id 반환)
 */
class UserPrincipal private constructor(
    private val user: User,
    private val attributes: Map<String, Any> = emptyMap()
) : UserDetails, OAuth2User {

    companion object {
        /**
         * 일반 로그인용 UserPrincipal 생성
         */
        fun create(user: User): UserPrincipal {
            return UserPrincipal(user)
        }

        /**
         * OAuth2 로그인용 UserPrincipal 생성
         */
        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            return UserPrincipal(user, attributes)
        }
    }

    // User 엔티티의 정보에 접근할 수 있는 프로퍼티들
    val id: Long get() = user.id
    val email: String get() = user.email
    val userName: String get() = user.username  // username -> userName으로 변경하여 충돌 방지
    val role: Role get() = user.role
    val provider: AuthProvider get() = user.authProvider

    /**
     * 원본 User 엔티티 반환 (비즈니스 로직에서 필요시 사용)
     */
    fun getUser(): User = user

    // ========== UserDetails 인터페이스 구현 ==========
    override fun getUsername(): String = user.email
    override fun getPassword(): String = user.password ?: ""
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = user.isActive

    // ========== OAuth2User 인터페이스 구현 ==========
    override fun getName(): String = user.id.toString()
    override fun getAttributes(): Map<String, Any> = attributes

    // ========== 편의 메서드들 ==========
    fun isLocalUser(): Boolean = user.authProvider == AuthProvider.LOCAL
    fun isSocialUser(): Boolean = user.authProvider != AuthProvider.LOCAL
    fun hasPassword(): Boolean = user.password != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserPrincipal) return false
        return user.id == other.user.id
    }

    override fun hashCode(): Int {
        return user.id.hashCode()
    }

    override fun toString(): String {
        return "UserPrincipal(id=${user.id}, email=${user.email}, role=${user.role})"
    }
}