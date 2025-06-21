package com.challkathon.momento.auth.exception

import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.global.exception.BaseException

// JWT 관련 예외
class JwtAuthenticationException(
    errorStatus: AuthErrorStatus
) : BaseException(errorStatus)

// 인증 관련 예외
class BadCredentialsException(
    message: String = "잘못된 인증 정보입니다"
) : BaseException(AuthErrorStatus._BAD_CREDENTIALS)

class UserNotFoundException(
    message: String = "사용자를 찾을 수 없습니다"
) : BaseException(AuthErrorStatus._USER_NOT_FOUND)

class EmailAlreadyExistsException(
    message: String = "이미 존재하는 이메일입니다"
) : BaseException(AuthErrorStatus._EMAIL_ALREADY_EXISTS)

// OAuth2 관련 예외
class OAuth2AuthenticationException(
    errorStatus: AuthErrorStatus,
    override val message: String? = null
) : BaseException(errorStatus)

// Refresh Token 관련 예외
class RefreshTokenException(
    errorStatus: AuthErrorStatus
) : BaseException(errorStatus)

// 계정 상태 관련 예외
class AccountNotVerifiedException(
    message: String = "이메일 인증이 필요합니다"
) : BaseException(AuthErrorStatus._ACCOUNT_NOT_VERIFIED)

class AccountDisabledException(
    message: String = "비활성화된 계정입니다"
) : BaseException(AuthErrorStatus._ACCOUNT_DISABLED)

// 권한 관련 예외
class InsufficientPrivilegesException(
    message: String = "권한이 부족합니다"
) : BaseException(AuthErrorStatus._INSUFFICIENT_PRIVILEGES)