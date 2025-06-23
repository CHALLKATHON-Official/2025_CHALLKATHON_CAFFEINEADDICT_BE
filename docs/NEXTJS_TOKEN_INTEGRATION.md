# Next.js JWT Token Integration Guide

## 토큰 전달 방식

### 개요
- **Access Token**: `Authorization: Bearer {token}` 헤더로만 전달
- **Refresh Token**: `refreshtoken` 이름의 httpOnly 쿠키로만 전달

## 1. Axios 설정

### 기본 설정
```javascript
// lib/axios.js
import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // 쿠키 자동 전송을 위해 필수
});

export default apiClient;
```

### 토큰 관리 유틸리티
```javascript
// utils/tokenManager.js
class TokenManager {
  constructor() {
    this.accessToken = null;
    this.isRefreshing = false;
    this.failedQueue = [];
  }

  setAccessToken(token) {
    this.accessToken = token;
    // Axios 기본 헤더에 설정
    if (token) {
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete apiClient.defaults.headers.common['Authorization'];
    }
  }

  getAccessToken() {
    return this.accessToken;
  }

  clearTokens() {
    this.accessToken = null;
    delete apiClient.defaults.headers.common['Authorization'];
  }

  processQueue(error, token = null) {
    this.failedQueue.forEach(({ resolve, reject }) => {
      if (error) {
        reject(error);
      } else {
        resolve(token);
      }
    });
    
    this.failedQueue = [];
  }
}

export const tokenManager = new TokenManager();
```

## 2. Axios Interceptor 설정

### 요청 인터셉터
```javascript
// lib/axios.js (continued)
import { tokenManager } from '../utils/tokenManager';

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    // Access Token이 있으면 헤더에 자동 추가
    const token = tokenManager.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
```

### 응답 인터셉터 (자동 토큰 갱신)
```javascript
// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => {
    // 응답에서 새로운 Access Token 추출 (토큰 갱신 시)
    const newToken = response.headers['authorization'];
    if (newToken && newToken.startsWith('Bearer ')) {
      const token = newToken.substring(7);
      tokenManager.setAccessToken(token);
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (tokenManager.isRefreshing) {
        // 이미 갱신 중이면 큐에 추가
        return new Promise((resolve, reject) => {
          tokenManager.failedQueue.push({ resolve, reject });
        }).then(() => {
          return apiClient(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      tokenManager.isRefreshing = true;

      try {
        // 토큰 갱신 시도
        const response = await apiClient.post('/api/v1/auth/refresh');
        const newToken = response.data.data.accessToken;
        
        tokenManager.setAccessToken(newToken);
        tokenManager.processQueue(null, newToken);
        
        // 원래 요청 재시도
        return apiClient(originalRequest);
      } catch (refreshError) {
        tokenManager.processQueue(refreshError, null);
        tokenManager.clearTokens();
        
        // 로그인 페이지로 리다이렉트
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        tokenManager.isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
```

## 3. 로그인 처리

### 카카오 로그인
```javascript
// pages/login.js
import { useEffect } from 'react';
import { useRouter } from 'next/router';
import { tokenManager } from '../utils/tokenManager';

export default function LoginPage() {
  const router = useRouter();

  const handleKakaoLogin = () => {
    // 카카오 OAuth2 로그인 시작
    window.location.href = `${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/kakao`;
  };

  return (
    <div>
      <h1>로그인</h1>
      <button onClick={handleKakaoLogin}>
        카카오로 로그인
      </button>
    </div>
  );
}
```

### OAuth2 콜백 처리
```javascript
// pages/oauth2/redirect.js
import { useEffect } from 'react';
import { useRouter } from 'next/router';
import { tokenManager } from '../../utils/tokenManager';

export default function OAuth2Redirect() {
  const router = useRouter();

  useEffect(() => {
    const { token, success, needsFamilyRole } = router.query;

    if (success === 'true' && token) {
      // Access Token 저장
      tokenManager.setAccessToken(token);
      
      if (needsFamilyRole === 'true') {
        // 가족 역할 선택 필요
        router.push('/family-role-selection');
      } else {
        // 로그인 완료
        router.push('/dashboard');
      }
    } else {
      // 로그인 실패
      router.push('/login?error=oauth_failed');
    }
  }, [router]);

  return <div>로그인 처리 중...</div>;
}
```

### 가족 역할 선택 페이지
```javascript
// pages/family-role-selection.js
import { useState } from 'react';
import { useRouter } from 'next/router';
import apiClient from '../lib/axios';

export default function FamilyRoleSelection() {
  const [selectedRole, setSelectedRole] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const roles = [
    { code: 'DAD', description: '아빠' },
    { code: 'MOM', description: '엄마' },
    { code: 'SON', description: '아들' },
    { code: 'DAUGHTER', description: '딸' }
  ];

  const handleSubmit = async () => {
    if (!selectedRole) return;

    setLoading(true);
    try {
      await apiClient.post('/api/v1/family-role/select', {
        familyRole: selectedRole
      });
      router.push('/dashboard');
    } catch (error) {
      console.error('가족 역할 선택 실패:', error);
      alert('가족 역할 선택에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>가족 역할 선택</h1>
      <div>
        {roles.map(role => (
          <label key={role.code}>
            <input
              type="radio"
              name="familyRole"
              value={role.code}
              checked={selectedRole === role.code}
              onChange={(e) => setSelectedRole(e.target.value)}
            />
            {role.description}
          </label>
        ))}
      </div>
      <button 
        onClick={handleSubmit} 
        disabled={!selectedRole || loading}
      >
        {loading ? '저장 중...' : '선택 완료'}
      </button>
    </div>
  );
}
```

## 4. 로그아웃 처리

```javascript
// utils/auth.js
import apiClient from '../lib/axios';
import { tokenManager } from './tokenManager';

export const logout = async () => {
  try {
    // 서버에 로그아웃 요청 (모든 토큰 무효화)
    await apiClient.post('/api/v1/auth/logout');
  } catch (error) {
    console.error('로그아웃 요청 실패:', error);
  } finally {
    // 클라이언트 토큰 정리
    tokenManager.clearTokens();
    // 로그인 페이지로 리다이렉트
    window.location.href = '/login';
  }
};

export const logoutDevice = async () => {
  try {
    // 현재 디바이스만 로그아웃
    await apiClient.post('/api/v1/auth/logout/device');
  } catch (error) {
    console.error('디바이스 로그아웃 실패:', error);
  } finally {
    tokenManager.clearTokens();
    window.location.href = '/login';
  }
};
```

## 5. 보안 고려사항

### 토큰 저장
- **Access Token**: 메모리에만 저장 (XSS 공격 방지)
- **Refresh Token**: httpOnly 쿠키 (JavaScript 접근 불가)

### CSRF 방지
- 모든 API 요청에 Authorization 헤더 사용
- 쿠키는 자동 전송되므로 CSRF 토큰 불필요

### 자동 갱신
- Access Token 만료 시 자동으로 갱신
- 갱신 실패 시 자동 로그아웃

## 6. 환경변수 설정

```bash
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080
# 운영환경에서는 HTTPS URL 사용
```

## 7. 사용 예시

```javascript
// 인증이 필요한 API 호출
const fetchUserData = async () => {
  try {
    const response = await apiClient.get('/api/v1/user/me');
    return response.data;
  } catch (error) {
    console.error('사용자 정보 조회 실패:', error);
    throw error;
  }
};
```

이 가이드를 따라 구현하면 안전하고 효율적인 JWT 토큰 관리 시스템을 구축할 수 있습니다.