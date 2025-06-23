package com.challkathon.momento.auth.dto.request

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    val password: String,

    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 2, max = 50, message = "사용자명은 2-50자 사이여야 합니다")
    val username: String,

    @field:NotBlank(message = "가족 내 역할은 필수입니다")
val familyRole: FamilyRole
)