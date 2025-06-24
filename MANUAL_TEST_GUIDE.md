# AI 질문 자동 생성 및 할당 시스템 수동 테스트 가이드

## 🚀 빠른 시작

### 1. 전체 테스트 실행
```bash
# 테스트 스크립트 실행 권한 부여
chmod +x scripts/test_ai_question_system.sh

# 전체 테스트 실행
./scripts/test_ai_question_system.sh
```

### 2. 개별 테스트 실행
```bash
# 스케줄러 테스트
./gradlew test --tests DailyQuestionSchedulerTest

# 통합 테스트
./gradlew test --tests SchedulerIntegrationTest

# 데이터 검증 테스트
./gradlew test --tests DataValidationTest
```

## 📋 상세 테스트 방법

### 1. 스케줄링 테스트 (새벽 2시, 오전 9시)

#### A. 자동 테스트
```bash
./gradlew test --tests DailyQuestionSchedulerTest
```

#### B. 수동 스케줄러 실행
```bash
# 애플리케이션 실행
./gradlew bootRun

# 다른 터미널에서 API 호출로 수동 실행
curl -X POST http://localhost:8080/api/v1/admin/scheduler/generate-questions
curl -X POST http://localhost:8080/api/v1/admin/scheduler/assign-questions
```

#### C. 스케줄러 로그 확인
```bash
# 로그 파일에서 스케줄러 실행 확인
tail -f logs/application.log | grep "DailyQuestionScheduler"
```

### 2. 기능 테스트

#### A. AI 질문 생성 테스트
```bash
# 질문 생성 매니저 테스트
./gradlew test --tests QuestionGenerationManagerTest

# 실제 AI API 호출 테스트 (API 키 필요)
./gradlew test --tests QuestionGenerationManagerTest --info
```

#### B. Family 할당 테스트
```bash
# 데이터 검증 테스트
./gradlew test --tests DataValidationTest

# 특정 테스트 메서드만 실행
./gradlew test --tests DataValidationTest.testQuestionAssignmentDataValidation
```

### 3. 데이터 검증

#### A. 데이터베이스 직접 확인
```sql
-- 생성된 질문 확인
SELECT * FROM question WHERE is_ai_generated = true ORDER BY generated_date DESC;

-- Family별 할당된 질문 확인
SELECT f.invite_code, q.content, fq.assigned_at, fq.status 
FROM family_question fq 
JOIN family f ON fq.family_id = f.id 
JOIN question q ON fq.question_id = q.id 
ORDER BY fq.assigned_at DESC;

-- 질문 카테고리 분포 확인
SELECT category, COUNT(*) as count 
FROM question 
WHERE is_ai_generated = true 
GROUP BY category;
```

#### B. REST API를 통한 확인
```bash
# 오늘의 질문 조회
curl -X GET http://localhost:8080/api/v1/questions/today \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 질문 히스토리 조회
curl -X GET http://localhost:8080/api/v1/questions/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. 예외 상황 테스트

#### A. AI API 실패 시뮬레이션
```bash
# 잘못된 API 키로 테스트
OPENAI_API_KEY=invalid_key ./gradlew test --tests ExceptionHandlingTest.testAiApiFailureHandling
```

#### B. 데이터베이스 연결 오류 시뮬레이션
```bash
# 데이터베이스 서버 중지 후 테스트
docker stop momento_mysql
./gradlew test --tests ExceptionHandlingTest.testDatabaseConnectionFailureHandling
docker start momento_mysql
```

#### C. 메모리 부족 시뮬레이션
```bash
# JVM 힙 크기 제한하여 테스트
./gradlew test --tests ExceptionHandlingTest.testOutOfMemoryHandling -Xmx64m
```

## 🔍 결과 확인 방법

### 1. 테스트 결과 로그
```bash
# 테스트 리포트 확인
open build/reports/tests/test/index.html

# 콘솔 로그 확인
./gradlew test --tests SchedulerIntegrationTest --info
```

### 2. 스케줄러 실행 로그
```bash
# 애플리케이션 로그에서 스케줄러 실행 확인
grep "DailyQuestionScheduler" logs/application.log

# 질문 생성 로그 확인
grep "Question generated" logs/application.log

# 할당 로그 확인
grep "Question assigned" logs/application.log
```

### 3. 데이터베이스 상태 확인
```sql
-- 최근 생성된 질문 확인
SELECT 
    content, 
    category, 
    is_ai_generated, 
    generated_date,
    usage_count
FROM question 
WHERE generated_date >= CURDATE() 
ORDER BY generated_date DESC;

-- Family별 할당 상태 확인
SELECT 
    f.invite_code,
    COUNT(fq.id) as assigned_questions,
    MAX(fq.assigned_at) as last_assignment
FROM family f
LEFT JOIN family_question fq ON f.id = fq.family_id
GROUP BY f.id, f.invite_code
ORDER BY last_assignment DESC;
```

## ⚠️ 주의사항

1. **API 키 설정**: OpenAI API 키가 application.yml에 올바르게 설정되어 있는지 확인
2. **데이터베이스 연결**: MySQL 서버가 실행 중인지 확인
3. **스케줄러 활성화**: `scheduler.enabled=true` 설정 확인
4. **시간대 설정**: 스케줄러 시간이 서버 시간대와 일치하는지 확인

## 🎯 성공 기준

### 질문 생성 성공 기준
- [x] 새벽 2시에 질문 풀이 자동 생성됨
- [x] 5개 카테고리별로 질문이 생성됨
- [x] 질문 내용이 10-200자 사이
- [x] AI 생성 플래그가 올바르게 설정됨

### 질문 할당 성공 기준
- [x] 오전 9시에 활성 Family들에게 질문이 할당됨
- [x] 중복 할당이 발생하지 않음
- [x] 할당된 질문의 상태가 ASSIGNED로 설정됨
- [x] 24시간 후 만료 시간이 설정됨

### 예외 처리 성공 기준
- [x] AI API 실패 시에도 시스템이 중단되지 않음
- [x] 데이터베이스 연결 오류 시 적절한 로깅
- [x] 빈 Family 목록에 대한 안전한 처리

## 📞 문제 해결

### 자주 발생하는 문제들

1. **OpenAI API 호출 실패**
   ```bash
   # API 키 확인
   echo $OPENAI_API_KEY
   
   # 테스트 API 호출
   curl -H "Authorization: Bearer $OPENAI_API_KEY" \
        https://api.openai.com/v1/models
   ```

2. **스케줄러가 실행되지 않음**
   ```bash
   # 스케줄러 설정 확인
   grep "scheduler.enabled" application.yml
   
   # 로그 레벨 증가
   logging.level.com.challkathon.momento.domain.question.scheduler=DEBUG
   ```

3. **데이터베이스 연결 실패**
   ```bash
   # MySQL 서버 상태 확인
   docker ps | grep momento_mysql
   
   # 연결 테스트
   mysql -h localhost -u root -p momento_db
   ```

이 가이드를 따라하면 AI 질문 자동 생성 및 할당 시스템의 모든 기능을 체계적으로 테스트할 수 있습니다.