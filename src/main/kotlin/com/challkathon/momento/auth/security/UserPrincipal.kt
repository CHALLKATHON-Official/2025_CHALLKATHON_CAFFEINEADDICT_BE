package com.challkathon.momento.auth.security

import com.challkathon.momento.domain.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class UserPrincipal(
    val id: Long,
    val email: String,
    val displayName: String,
    private val password: String?,
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any> = mutableMapOf()
) : UserDetails, OAuth2User {

    override fun getPassword(): String? = password
    override fun getUsername(): String = email
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    // OAuth2User interface
    override fun getAttributes(): Map<String, Any> = attributes
    override fun getName(): String = id.toString()

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            
            return UserPrincipal(
                id = user.id,
                email = user.email,
                displayName = user.username,
                password = user.password,
                authorities = authorities
            )
        }

        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            val userPrincipal = create(user)
            return UserPrincipal(
                id = userPrincipal.id,
                email = userPrincipal.email,
                displayName = userPrincipal.displayName,
                password = userPrincipal.password,
                authorities = userPrincipal.authorities,
                attributes = attributes
            )
        }
    }
}