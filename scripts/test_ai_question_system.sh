#!/bin/bash

echo "🤖 AI 질문 자동 생성 및 할당 시스템 테스트 시작"
echo "=================================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 테스트 결과 저장
TEST_RESULTS=()

run_test() {
    local test_name=$1
    local test_command=$2
    
    echo -e "\n${BLUE}🧪 $test_name${NC}"
    echo "----------------------------------------"
    
    if eval $test_command; then
        echo -e "${GREEN}✅ $test_name 성공${NC}"
        TEST_RESULTS+=("✅ $test_name")
    else
        echo -e "${RED}❌ $test_name 실패${NC}"
        TEST_RESULTS+=("❌ $test_name")
    fi
}

# 1. 스케줄러 테스트
run_test "스케줄러 유닛 테스트" "./gradlew test --tests DailyQuestionSchedulerTest"

# 2. 통합 테스트 - 스케줄러 수동 실행
run_test "스케줄러 통합 테스트" "./gradlew test --tests SchedulerIntegrationTest"

# 3. 기능 테스트
run_test "질문 생성 매니저 테스트" "./gradlew test --tests QuestionGenerationManagerTest"

# 4. 데이터 검증 테스트  
run_test "데이터 검증 통합 테스트" "./gradlew test --tests DataValidationTest"

# 5. 예외 처리 테스트
run_test "예외 상황 처리 테스트" "./gradlew test --tests ExceptionHandlingTest"

# 6. 전체 질문 관련 테스트 실행
run_test "전체 질문 시스템 테스트" "./gradlew test --tests '*Question*'"

# 결과 요약
echo -e "\n${YELLOW}📊 테스트 결과 요약${NC}"
echo "=================================================="

for result in "${TEST_RESULTS[@]}"; do
    echo -e "$result"
done

# 성공/실패 카운트
SUCCESS_COUNT=$(printf '%s\n' "${TEST_RESULTS[@]}" | grep -c "✅")
TOTAL_COUNT=${#TEST_RESULTS[@]}

echo -e "\n${BLUE}총 테스트: $TOTAL_COUNT개${NC}"
echo -e "${GREEN}성공: $SUCCESS_COUNT개${NC}"
echo -e "${RED}실패: $((TOTAL_COUNT - SUCCESS_COUNT))개${NC}"

if [ $SUCCESS_COUNT -eq $TOTAL_COUNT ]; then
    echo -e "\n${GREEN}🎉 모든 테스트가 성공했습니다!${NC}"
    exit 0
else
    echo -e "\n${RED}⚠️  일부 테스트가 실패했습니다.${NC}"
    exit 1
fi