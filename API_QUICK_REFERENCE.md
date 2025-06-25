# 🚀 Momento AI API 빠른 참조 가이드

## 🔑 인증 플로우
```bash
1. 카카오 로그인: GET /oauth2/authorization/kakao
2. 콜백 처리: GET /login/oauth2/code/kakao?code={code}
3. JWT 토큰 자동 쿠키 설정 (accessToken, refreshToken)
```

## 📡 API 호출 예시

### cURL
```bash
# AI 질문 생성
curl -X POST https://dev.caffeineoverdose.shop/api/v1/questions/generate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Postman
1. Method: `POST`
2. URL: `https://dev.caffeineoverdose.shop/api/v1/questions/generate`
3. Headers:
   - Authorization: `Bearer YOUR_JWT_TOKEN`
   - Content-Type: `application/json`

### JavaScript (Fetch API)
```javascript
// 간단한 호출
const response = await fetch('/api/v1/questions/generate', {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});
const data = await response.json();
console.log(data.data.content); // 생성된 질문 내용
console.log(data.data.category); // 질문 카테고리
console.log(data.data.id); // 질문 ID
```

### Axios
```javascript
const { data } = await axios.post(
    '/api/v1/questions/generate',
    {},
    {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    }
);
console.log(data.data.content); // 생성된 질문 내용
console.log(data.data.category); // 질문 카테고리
```

## 📋 응답 예시

### 성공
```json
{
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공",
    "data": {
        "id": 123,
        "content": "오늘 가족과 함께한 시간 중 가장 행복했던 순간은 언제인가요?",
        "category": "DAILY",
        "isAIGenerated": true,
        "generatedAt": "2025-01-15T10:30:00"
    }
}
```

### 에러 코드
| 코드 | 상태 | 설명 |
|------|------|------|
| 400 | Bad Request | 일일 한도 초과 (5개) |
| 401 | Unauthorized | JWT 토큰 없음/만료 |
| 404 | Not Found | 사용자 정보 없음 |
| 500 | Internal Error | AI 생성 실패 |

## 🛠️ 디버깅 팁

1. **토큰 확인**
```javascript
console.log(document.cookie); // 쿠키 확인
```

2. **네트워크 탭 확인**
- Chrome DevTools > Network
- Request Headers 확인
- Response 상태 코드 확인

3. **에러 로깅**
```javascript
try {
    const data = await generateAIQuestion();
} catch (error) {
    console.error('Error details:', {
        message: error.message,
        status: error.status,
        response: error.response
    });
}
```

## ⚡ 성능 최적화

- 질문 생성은 2-3초 소요
- 결과를 캐싱하여 반복 호출 방지
- 디바운싱으로 중복 클릭 방지

## 🔗 관련 문서

- [전체 API 가이드](./API_USAGE_GUIDE.md)
- [아키텍처 문서](./ARCHITECTURE.md)
- [AI 시스템 가이드](./AI_QUESTION_SYSTEM_GUIDE.md)
