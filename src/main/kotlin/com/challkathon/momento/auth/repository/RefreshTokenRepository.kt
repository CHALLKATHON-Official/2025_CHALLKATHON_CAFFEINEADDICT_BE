package com.challkathon.momento.auth.repository

import com.challkathon.momento.auth.entity.RefreshToken
import com.challkathon.momento.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    
    fun findByTokenAndIsRevokedFalse(token: String): Optional<RefreshToken>
    
    fun findByUser(user: User): List<RefreshToken>
    
    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.user = :user")
    fun revokeAllByUser(user: User): Int
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < CURRENT_TIMESTAMP OR r.isRevoked = true")
    fun deleteExpiredAndRevokedTokens(): Int
}