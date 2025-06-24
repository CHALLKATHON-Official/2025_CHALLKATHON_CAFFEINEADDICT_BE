package com.challkathon.momento.auth.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class AuthErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {

    // 인증 관련 에러
    _JWT_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4001", "JWT 토큰 인증에 실패했습니다."),
    _TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4002", "토큰이 만료되었습니다."),
    _TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4003", "유효하지 않은 토큰입니다."),
    _TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH4004", "토큰 형식이 올바르지 않습니다."),
    _TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "AUTH4005", "지원하지 않는 토큰입니다."),
    _TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4006", "토큰 서명이 유효하지 않습니다."),

    // 자격 증명 관련 에러
    _BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4010", "이메일 또는 비밀번호가 올바르지 않습니다."),
    _USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4011", "사용자를 찾을 수 없습니다."),
    _AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4012", "인증에 실패했습니다."),
    _ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH4013", "접근 권한이 없습니다."),
    _ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "AUTH4014", "비활성화된 계정입니다."),
    _ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH4015", "잠긴 계정입니다."),
    _CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4016", "자격 증명이 만료되었습니다."),

    // 회원가입 관련 에러
    _EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH4020", "이미 존재하는 이메일입니다."),
    _EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "AUTH4021", "올바르지 않은 이메일 형식입니다."),
    _PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "AUTH4022", "비밀번호가 너무 약합니다."),
    _PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "AUTH4023", "비밀번호는 최소 6자 이상이어야 합니다."),
    _NAME_TOO_SHORT(HttpStatus.BAD_REQUEST, "AUTH4024", "이름은 최소 2자 이상이어야 합니다."),
    _NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "AUTH4025", "이름은 최대 50자까지 가능합니다."),

    // OAuth2 관련 에러
    _OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4030", "소셜 로그인 인증에 실패했습니다."),
    _OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH4031", "지원하지 않는 소셜 로그인 제공자입니다."),
    _OAUTH2_USER_INFO_RETRIEVAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH4032", "소셜 로그인 사용자 정보를 가져오는데 실패했습니다."),
    _OAUTH2_CALLBACK_ERROR(HttpStatus.BAD_REQUEST, "AUTH4033", "소셜 로그인 콜백 처리 중 오류가 발생했습니다."),
    _OAUTH2_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH4034", "OAuth2 state 파라미터가 일치하지 않습니다."),

    // 토큰 관리 관련 에러
    _REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4040", "Refresh Token을 찾을 수 없습니다."),
    _REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4041", "Refresh Token이 만료되었습니다."),
    _REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4042", "유효하지 않은 Refresh Token입니다."),
    _ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH4043", "Access Token이 필요합니다."),
    _BEARER_TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH4044", "Bearer 토큰 형식이 올바르지 않습니다."),

    // 권한 관련 에러
    _INSUFFICIENT_PRIVILEGES(HttpStatus.FORBIDDEN, "AUTH4050", "권한이 부족합니다."),
    _ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "AUTH4051", "관리자 권한이 필요합니다."),
    _USER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "AUTH4052", "사용자 권한이 필요합니다."),

    // 계정 상태 관련 에러
    _ACCOUNT_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH4060", "이메일 인증이 필요합니다."),
    _ACCOUNT_SUSPENDED(HttpStatus.UNAUTHORIZED, "AUTH4061", "정지된 계정입니다."),
    _ACCOUNT_DELETED(HttpStatus.UNAUTHORIZED, "AUTH4062", "삭제된 계정입니다."),

    // 보안 관련 에러
    _TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH4070", "로그인 시도 횟수를 초과했습니다."),
    _SUSPICIOUS_ACTIVITY_DETECTED(HttpStatus.UNAUTHORIZED, "AUTH4071", "의심스러운 활동이 감지되었습니다."),
    _IP_BLOCKED(HttpStatus.FORBIDDEN, "AUTH4072", "차단된 IP에서의 접근입니다."),

    // 검증 관련 에러
    _VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH4080", "입력 데이터 검증에 실패했습니다."),
    _REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "AUTH4081", "필수 필드가 누락되었습니다."),
    _INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "AUTH4082", "입력 형식이 올바르지 않습니다."),

    // 가족 역할 에러
    _ALREADY_SELECT_FAMILY_ROLE(HttpStatus.BAD_REQUEST, "AUTH4090", "이미 가족 역할이 선택되었습니다."),
    
    // 추가 토큰 관련 에러
    _REFRESH_TOKEN_VERSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4091", "Refresh Token 버전이 없습니다."),
    _SECURITY_VIOLATION(HttpStatus.FORBIDDEN, "AUTH4092", "보안 위반: 모든 세션이 무효화되었습니다."),
    _TOKEN_VERSION_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH4093", "토큰 버전이 일치하지 않습니다."),
    
    // AI 서비스 에러
    _AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH4094", "AI 서비스 오류가 발생했습니다.")
    ;

    private val isSuccess = false

    override fun getCode(): BaseCode {
        return BaseCode(
            httpStatus = httpStatus,
            isSuccess = isSuccess,
            code = code,
            message = message
        )
    }
}