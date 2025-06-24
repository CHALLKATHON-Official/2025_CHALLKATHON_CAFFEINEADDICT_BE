package com.challkathon.momento.domain.family.repository

import com.challkathon.momento.domain.family.entity.Family
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FamilyRepository : JpaRepository<Family, Long>, FamilyRepositoryCustom {
    fun findByInviteCode(inviteCode: String): Family?
}
