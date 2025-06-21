package com.challkathon.momento.domain.user.repository

import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findByEmailAndAuthProvider(email: String, provider: AuthProvider): Optional<User>
    fun existsByEmailAndAuthProvider(email: String, provider: AuthProvider): Boolean
    fun findByProviderId(providerId: String): Optional<User>
    fun findAllByIsActiveTrue(): List<User>
    fun findByEmailAndIsActiveTrue(email: String): Optional<User>
    fun findByRefreshTokenIsNotNull(): List<User>
}