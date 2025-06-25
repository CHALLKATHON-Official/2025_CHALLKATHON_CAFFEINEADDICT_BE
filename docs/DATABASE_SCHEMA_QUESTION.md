# 📊 Momento 데이터베이스 스키마 - 질문 시스템

## 🗄️ 테이블 구조

### 1. question 테이블
AI가 생성하거나 사전 정의된 모든 질문을 저장하는 테이블입니다.

```sql
CREATE TABLE question (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL COMMENT '질문 내용',
    category VARCHAR(20) NOT NULL COMMENT '질문 카테고리',
    is_ai_generated BOOLEAN NOT NULL DEFAULT false COMMENT 'AI 생성 여부',
    generated_date DATE COMMENT '생성 날짜',
    usage_count INT DEFAULT 0 COMMENT '사용 횟수',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_generated_date (generated_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. user_question 테이블
사용자와 질문 간의 매핑을 관리하는 테이블입니다.

```sql
CREATE TABLE user_question (
    user_question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    question_id BIGINT NOT NULL COMMENT '질문 ID',
    ai_model VARCHAR(50) COMMENT '사용된 AI 모델',
    is_answered BOOLEAN NOT NULL DEFAULT false COMMENT '답변 여부',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id),
    INDEX idx_user_question_user (user_id),
    INDEX idx_user_question_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. answer 테이블 (기존)
사용자의 답변을 저장하는 테이블입니다.

```sql
CREATE TABLE answer (
    answer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    content TEXT NOT NULL COMMENT '답변 내용',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id),
    INDEX idx_user_answer (user_id),
    INDEX idx_question_answer (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 🔗 테이블 관계도

```
user (1) ─────┬──── (N) user_question
              │
              └──── (N) answer
                         │
question (1) ────────────┼──── (N) user_question
              │          │
              └──────────┴──── (N) answer
```

## 📝 카테고리 enum 값

```kotlin
enum class QuestionCategory {
    MEMORY,     // 추억
    DAILY,      // 일상
    FUTURE,     // 미래
    GRATITUDE,  // 감사
    GENERAL     // 일반
}
```

## 💾 데이터 예시

### question 테이블
```sql
INSERT INTO question (content, category, is_ai_generated, generated_date) VALUES
('오늘 가족과 함께한 시간 중 가장 행복했던 순간은 언제인가요?', 'DAILY', true, '2025-01-15'),
('가족과 함께 다시 가고 싶은 장소는 어디인가요?', 'MEMORY', true, '2025-01-15'),
('올해 가족과 함께 이루고 싶은 목표가 있나요?', 'FUTURE', false, '2025-01-15');
```

### user_question 테이블
```sql
INSERT INTO user_question (user_id, question_id, ai_model, is_answered) VALUES
(1, 1, 'gpt-4o-mini', false),
(1, 2, 'gpt-4o-mini', true),
(2, 1, 'gpt-4', false);
```

## 🔍 주요 쿼리

### 사용자의 최근 질문 조회
```sql
SELECT uq.*, q.content, q.category 
FROM user_question uq
JOIN question q ON uq.question_id = q.question_id
WHERE uq.user_id = ?
AND uq.created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY uq.created_at DESC;
```

### 중복 질문 확인
```sql
SELECT COUNT(*) > 0 
FROM user_question uq
JOIN question q ON uq.question_id = q.question_id
WHERE uq.user_id = ?
AND q.content = ?
AND uq.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY);
```

### 카테고리별 질문 통계
```sql
SELECT q.category, COUNT(*) as count
FROM question q
JOIN user_question uq ON q.question_id = uq.question_id
WHERE uq.user_id = ?
GROUP BY q.category;
```

## 🚀 마이그레이션 스크립트

```sql
-- V1__create_question_tables.sql
CREATE TABLE IF NOT EXISTS question (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    category VARCHAR(20) NOT NULL,
    is_ai_generated BOOLEAN NOT NULL DEFAULT false,
    generated_date DATE,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_generated_date (generated_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_question (
    user_question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    ai_model VARCHAR(50),
    is_answered BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id),
    INDEX idx_user_question_user (user_id),
    INDEX idx_user_question_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
