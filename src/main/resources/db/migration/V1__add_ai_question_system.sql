-- AI 질문 생성 시스템을 위한 데이터베이스 마이그레이션
-- 실행 날짜: 2025-01-01

-- 1. Question 테이블 수정
ALTER TABLE question 
ADD COLUMN IF NOT EXISTS category VARCHAR(20) DEFAULT 'GENERAL',
ADD COLUMN IF NOT EXISTS is_ai_generated BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS generated_date DATE,
ADD COLUMN IF NOT EXISTS usage_count INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_question_category ON question(category);
CREATE INDEX IF NOT EXISTS idx_question_generated_date ON question(generated_date);
CREATE INDEX IF NOT EXISTS idx_question_usage_count ON question(usage_count);

-- 2. FamilyQuestion 테이블 수정
ALTER TABLE family_question
ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS due_date TIMESTAMP,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ASSIGNED',
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_family_question_status ON family_question(status);
CREATE INDEX IF NOT EXISTS idx_family_question_assigned_at ON family_question(assigned_at);
CREATE INDEX IF NOT EXISTS idx_family_question_due_date ON family_question(due_date);
CREATE INDEX IF NOT EXISTS idx_family_question_family_id ON family_question(family_id);

-- 3. User 테이블에 last_active_at 컬럼 추가 (활성 가족 조회용)
ALTER TABLE users
ADD COLUMN IF NOT EXISTS last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_user_last_active_at ON users(last_active_at);

-- 4. 기본 질문 데이터 삽입 (선택사항)
INSERT INTO question (content, category, is_ai_generated, generated_date, usage_count) VALUES
('오늘 가장 감사했던 순간은 무엇인가요?', 'GRATITUDE', FALSE, CURRENT_DATE, 0),
('가족과 함께한 가장 행복했던 추억은 무엇인가요?', 'MEMORY', FALSE, CURRENT_DATE, 0),
('오늘 하루 중 가장 기억에 남는 일은 무엇인가요?', 'DAILY', FALSE, CURRENT_DATE, 0),
('올해 가족과 함께 꼭 해보고 싶은 일이 있다면?', 'FUTURE', FALSE, CURRENT_DATE, 0),
('가족에게 전하고 싶은 따뜻한 말 한마디는?', 'GRATITUDE', FALSE, CURRENT_DATE, 0)
ON DUPLICATE KEY UPDATE usage_count = usage_count;
