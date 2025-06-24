# AI 질문 생성 시스템 데이터베이스 검증 가이드

## 🎯 목적
실제 AI가 생성한 질문들이 데이터베이스에 올바르게 저장되는지 확인하기 위한 가이드입니다.

## 🚀 빠른 시작

### 1. 실제 데이터 생성 테스트 실행
```bash
# 롤백이 비활성화된 실제 AI 호출 테스트 실행
./gradlew test --tests "RealAIQuestionIntegrationTest"
```

### 2. 애플리케이션 실행 (H2 Console 접속용)
```bash
# 백그라운드로 애플리케이션 실행
./gradlew bootRun &

# 또는 포어그라운드 실행
./gradlew bootRun
```

### 3. H2 Console 접속
1. 브라우저에서 접속: http://localhost:8080/h2-console
2. 연결 정보 입력:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **User Name**: `sa`
   - **Password**: (비어둠)
3. "Connect" 버튼 클릭

## 📊 데이터 확인 방법

### A. 기본 데이터 확인

#### 1. 전체 질문 조회
```sql
SELECT 
    id,
    content,
    category,
    is_ai_generated as ai_generated,
    generated_date,
    usage_count,
    created_at
FROM question
ORDER BY created_at DESC;
```

#### 2. AI 생성 질문만 조회
```sql
SELECT 
    content,
    category,
    generated_date,
    usage_count
FROM question 
WHERE is_ai_generated = true
ORDER BY generated_date DESC, category;
```

#### 3. 오늘 생성된 AI 질문
```sql
SELECT 
    content,
    category,
    generated_date,
    created_at
FROM question 
WHERE is_ai_generated = true 
AND generated_date = CURRENT_DATE
ORDER BY category, created_at;
```

### B. 통계 및 분석

#### 4. 카테고리별 질문 분포
```sql
SELECT 
    category,
    COUNT(*) as question_count,
    MIN(generated_date) as first_generated,
    MAX(generated_date) as last_generated
FROM question 
WHERE is_ai_generated = true
GROUP BY category
ORDER BY question_count DESC;
```

#### 5. AI 질문 생성 현황 요약
```sql
SELECT 
    'Total Questions' as metric,
    COUNT(*) as count
FROM question
UNION ALL
SELECT 
    'AI Generated Questions' as metric,
    COUNT(*) as count
FROM question 
WHERE is_ai_generated = true
UNION ALL
SELECT 
    'Today AI Questions' as metric,
    COUNT(*) as count
FROM question 
WHERE is_ai_generated = true 
AND generated_date = CURRENT_DATE;
```

### C. Family 할당 확인

#### 6. Family별 할당된 질문 조회
```sql
SELECT 
    f.invite_code,
    f.count as family_size,
    q.content,
    q.category,
    fq.assigned_at,
    fq.status
FROM family_question fq
JOIN family f ON fq.family_id = f.id
JOIN question q ON fq.question_id = q.id
WHERE q.is_ai_generated = true
ORDER BY fq.assigned_at DESC;
```

## 🔍 검증 포인트

### ✅ 확인해야 할 사항들

1. **AI 생성 플래그**
   - `is_ai_generated` 필드가 `true`로 설정되어 있는지
   
2. **생성 날짜**
   - `generated_date` 필드가 오늘 날짜로 설정되어 있는지
   
3. **카테고리 분포**
   - 5개 카테고리(MEMORY, DAILY, FUTURE, GRATITUDE, GENERAL) 모두에 질문이 생성되었는지
   
4. **질문 품질**
   - 질문 내용이 적절한 길이(10-200자)인지
   - 한국어로 올바르게 생성되었는지
   - 가족 대화에 적합한 내용인지

5. **데이터 무결성**
   - 중복된 질문이 없는지
   - 필수 필드들이 모두 채워져 있는지

## 🛠️ 트러블슈팅

### 문제 1: AI 질문이 생성되지 않음
**증상**: `is_ai_generated = true` 인 질문이 없음
**해결방법**:
1. OpenAI API 키 확인: `application-test.yml`의 `openai.api.key` 설정 확인
2. 네트워크 연결 확인
3. API 호출 로그 확인

### 문제 2: H2 Console 접속 안됨
**증상**: H2 Console 페이지 로드 실패
**해결방법**:
1. 애플리케이션이 실행 중인지 확인: `./gradlew bootRun`
2. 포트 8080이 사용 중인지 확인: `lsof -i :8080`
3. `application-test.yml`에서 H2 Console 설정 확인

### 문제 3: 데이터가 롤백됨
**증상**: 테스트 후 데이터가 사라짐
**해결방법**:
1. `@Rollback(false)` 어노테이션 확인
2. `RealAIQuestionIntegrationTest` 클래스 사용
3. 트랜잭션 설정 확인

## 📈 예상 결과

### 성공적인 테스트 결과
```
🤖 실제 AI API를 호출하여 질문 풀 생성 시작...
📊 질문 생성 전 총 질문 수: 0
📊 질문 생성 후 총 질문 수: 15

🎯 생성된 AI 질문 목록:
1. [MEMORY] 어린 시절 가장 기억에 남는 가족 여행은 어디였나요?
2. [DAILY] 오늘 하루 중 가장 감사했던 순간은 무엇인가요?
3. [FUTURE] 우리 가족이 함께 이루고 싶은 꿈이 있다면?
...

📈 카테고리별 질문 분포:
  추억: 3개
  일상: 3개
  미래: 3개
  감사: 3개
  일반: 3개

✅ 실제 AI 질문 생성 및 DB 저장 테스트 완료!
```

## 🔗 추가 리소스

- **SQL 쿼리 모음**: `src/test/resources/database-queries.sql`
- **테스트 설정**: `src/test/resources/application-test.yml`
- **실제 데이터 테스트**: `RealAIQuestionIntegrationTest.kt`

이 가이드를 따라 실제 AI가 생성한 질문들이 데이터베이스에 올바르게 저장되는지 확인할 수 있습니다! 🎯