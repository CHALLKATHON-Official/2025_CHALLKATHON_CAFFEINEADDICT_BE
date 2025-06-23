package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.dto.response.FamilyRoleOption
import com.challkathon.momento.auth.dto.response.FamilyRoleStatusResponse
import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import com.challkathon.momento.domain.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class FamilyRoleService(
    private val userRepository: UserRepository
) {

    fun selectFamilyRole(email: String, familyRole: FamilyRole) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다.") }

        if (user.familyRoleSelected) {
            throw RuntimeException("이미 가족 역할이 선택되었습니다.")
        }

        user.updateFamilyRole(familyRole)
        userRepository.save(user)

        log.info { "사용자 ${user.email}의 가족 역할이 ${familyRole}로 설정되었습니다." }
    }

    @Transactional(readOnly = true)
    fun getFamilyRoleStatus(email: String): FamilyRoleStatusResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다.") }

        return FamilyRoleStatusResponse(
            familyRoleSelected = user.familyRoleSelected,
            familyRole = user.familyRole?.name,
            availableRoles = FamilyRole.values().map { 
                FamilyRoleOption(
                    code = it.name,
                    description = it.description
                )
            }
        )
    }
}