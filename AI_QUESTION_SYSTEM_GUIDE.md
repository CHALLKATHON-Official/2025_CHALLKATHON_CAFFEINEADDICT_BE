# AI 질문 생성 시스템 가이드

## 🤖 개요

Momento의 AI 질문 생성 시스템은 OpenAI Assistant API를 활용하여 매일 가족 구성원들에게 맞춤형 질문을 자동으로 생성하고 할당합니다.

## 🏗️ 시스템 아키텍처

### 핵심 구성 요소

1. **Assistant Service**: OpenAI Assistant API와 통신
2. **Question Generation Manager**: 캐싱 및 질문 풀 관리
3. **Family Context Analyzer**: 가족 특성 분석
4. **Daily Scheduler**: 자동 질문 생성 및 할당

### 3단계 질문 제공 전략

```
Level 1: 캐시 확인 (0원)
    ↓ (miss)
Level 2: 사전 생성 풀 (0원)
    ↓ (부족)
Level 3: AI 실시간 생성 ($0.01~0.03)
```

## 🚀 빠른 시작

### 1. 환경 변수 설정

```bash
# .env 파일 또는 IntelliJ Run Configuration
OPENAI_API_KEY=sk-...
REDIS_HOST=localhost
REDIS_PORT=6379
SCHEDULER_ENABLED=false  # 개발 환경에서는 false
```

### 2. 데이터베이스 마이그레이션

```sql
-- src/main/resources/db/migration/V1__add_ai_question_system.sql 실행
```

### 3. Redis 실행

```bash
# Docker 사용
docker run -d -p 6379:6379 redis:alpine

# 또는 로컬 설치
redis-server
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

## 📡 API 엔드포인트

### 오늘의 질문 조회

```http
GET /api/v1/questions/today
Authorization: Bearer {JWT_TOKEN}

Response:
{
  "success": true,
  "code": "SUCCESS",
  "message": "성공",
  "data": [
    {
      "id": 1,
      "questionId": 101,
      "content": "오늘 가장 감사했던 순간은 무엇인가요?",
      "category": "GRATITUDE",
      "assignedAt": "2025-01-01T09:00:00",
      "dueDate": "2025-01-02T09:00:00",
      "status": "ASSIGNED",
      "answerCount": 0,
      "answerRate": 0.0,
      "isOverdue": false
    }
  ]
}
```

### 질문 히스토리 조회

```http
GET /api/v1/questions/history?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {JWT_TOKEN}
```

### 질문 재생성

```http
POST /api/v1/questions/{questionId}/regenerate
Authorization: Bearer {JWT_TOKEN}
```

## ⚙️ 스케줄러 설정

### application.yml

```yaml
scheduler:
  question-generation:
    enabled: true  # 운영 환경에서만 true
    cron: "0 0 9 * * *"  # 매일 오전 9시
    timezone: Asia/Seoul
```

### 스케줄러 작업

1. **매일 오전 9시**: 활성 가족에게 질문 생성 및 할당
2. **매일 새벽 2시**: 질문 풀 사전 생성
3. **매일 자정**: 만료된 질문 상태 업데이트

## 💡 OpenAI Assistant 관리

### Assistant 초기 생성

Assistant는 애플리케이션 시작 시 자동으로 생성되거나 업데이트됩니다.

```kotlin
// 환경변수로 기존 Assistant ID 지정 가능
OPENAI_ASSISTANT_ID=asst_xxx
```

### 프롬프트 커스터마이징

`application.yml`에서 시스템 프롬프트를 수정할 수 있습니다:

```yaml
ai:
  prompt:
    system-instructions: |
      # 커스텀 프롬프트
```

## 📊 모니터링

### 로그 확인

```bash
# AI 질문 생성 로그
grep "question generation" logs/application.log

# 캐시 히트율 확인
grep "Cache hit\|Pool hit" logs/application.log
```

### 비용 예측

- **가족당 일일 비용**: ~$0.01 (3개 질문)
- **100가족 월간 비용**: ~$30
- **캐싱 적용 시**: ~$2-3 (90% 절감)

## 🔧 문제 해결

### OpenAI API 오류

```
Error: AI Assistant 초기화 실패
해결: OPENAI_API_KEY 환경변수 확인
```

### Redis 연결 오류

```
Error: Unable to connect to Redis
해결: Redis 서버 실행 확인
```

### 스케줄러 미작동

```
확인사항:
1. scheduler.question-generation.enabled=true
2. @EnableScheduling 어노테이션 확인
3. 서버 시간대 설정 확인
```

## 🚦 운영 가이드

### 프로덕션 체크리스트

- [ ] OpenAI API 키 설정
- [ ] Redis 클러스터 구성
- [ ] 스케줄러 활성화
- [ ] 모니터링 대시보드 설정
- [ ] 비용 알림 설정

### 성능 최적화

1. **캐시 TTL 조정**: 가족 활동 패턴에 따라 조정
2. **질문 풀 크기**: 카테고리별 20-50개 유지
3. **배치 처리**: 대량 가족 처리 시 배치 사용

## 📈 향후 개선 계획

1. **개인화 강화**: 답변 패턴 학습
2. **다국어 지원**: 영어, 중국어 등
3. **음성 질문**: TTS 연동
4. **감정 분석**: 답변 톤 분석

---

문의사항이나 버그 리포트는 GitHub Issues에 남겨주세요.
