-- AI 질문 생성 시스템 데이터 확인용 SQL 쿼리들
-- H2 Console에서 실행하여 데이터를 확인할 수 있습니다

-- 1. 전체 질문 조회 (AI 생성 여부별)
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

-- 2. AI 생성 질문만 조회
SELECT 
    id,
    content,
    category,
    generated_date,
    usage_count
FROM question 
WHERE is_ai_generated = true
ORDER BY generated_date DESC, category;

-- 3. 오늘 생성된 AI 질문 조회
SELECT 
    content,
    category,
    generated_date,
    created_at
FROM question 
WHERE is_ai_generated = true 
AND generated_date = CURRENT_DATE
ORDER BY category, created_at;

-- 4. 카테고리별 AI 질문 통계
SELECT 
    category,
    COUNT(*) as question_count,
    MIN(generated_date) as first_generated,
    MAX(generated_date) as last_generated
FROM question 
WHERE is_ai_generated = true
GROUP BY category
ORDER BY question_count DESC;

-- 5. Family별 할당된 질문 조회
SELECT 
    f.invite_code,
    f.count as family_size,
    q.content,
    q.category,
    fq.assigned_at,
    fq.due_date,
    fq.status
FROM family_question fq
JOIN family f ON fq.family_id = f.id
JOIN question q ON fq.question_id = q.id
WHERE q.is_ai_generated = true
ORDER BY fq.assigned_at DESC;

-- 6. 최근 활동 Family 조회
SELECT 
    invite_code,
    count as family_size,
    created_at,
    updated_at
FROM family
ORDER BY updated_at DESC;

-- 7. AI 질문 생성 현황 요약
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
AND generated_date = CURRENT_DATE
UNION ALL
SELECT 
    'Total Families' as metric,
    COUNT(*) as count
FROM family;

-- 8. 질문 사용 빈도 분석
SELECT 
    content,
    category,
    usage_count,
    generated_date
FROM question 
WHERE is_ai_generated = true
ORDER BY usage_count DESC, generated_date DESC
LIMIT 10;

-- 9. Family별 질문 할당 통계
SELECT 
    f.invite_code,
    f.count as family_size,
    COUNT(fq.id) as assigned_questions,
    MAX(fq.assigned_at) as last_assignment
FROM family f
LEFT JOIN family_question fq ON f.id = fq.family_id
GROUP BY f.id, f.invite_code, f.count
ORDER BY assigned_questions DESC;

-- 10. 오늘의 AI 질문 상세 정보
SELECT 
    q.content,
    q.category,
    q.generated_date,
    q.usage_count,
    COUNT(fq.id) as assigned_count
FROM question q
LEFT JOIN family_question fq ON q.id = fq.question_id
WHERE q.is_ai_generated = true 
AND q.generated_date = CURRENT_DATE
GROUP BY q.id, q.content, q.category, q.generated_date, q.usage_count
ORDER BY q.category;