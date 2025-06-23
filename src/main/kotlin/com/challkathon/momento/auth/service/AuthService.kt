package com.challkathon.momento.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService {
    // 이 서비스는 카카오 OAuth2만 지원하므로 비워둠
    // 필요시 추후 OAuth2 관련 로직 추가 가능
}