package com.challkathon.momento.domain.todo.service

import com.challkathon.momento.domain.todo.entity.enums.TodoCategory
import mu.KotlinLogging
// Simple hardcoded todo generation for now
// import com.challkathon.momento.domain.question.ai.AssistantService
import org.springframework.stereotype.Service

@Service
class TodoGeneratorService {
    // TODO: AssistantService integration
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * AI를 통해 가족 버킷리스트 Todo 생성
     */
    fun generateBucketListTodos(category: TodoCategory, count: Int): List<String> {
        logger.info { "AI를 통한 ${category.description} 버킷리스트 생성 시작 (${count}개)" }
        
        val prompt = buildTodoGenerationPrompt(category, count)
        
        // Temporary hardcoded todos for testing
        return getHardcodedTodos(category, count)
    }
    
    /**
     * 가족 컨텍스트를 고려한 맞춤형 버킷리스트 생성
     */
    fun generatePersonalizedBucketList(
        familyContext: String,
        count: Int = 3
    ): List<String> {
        logger.info { "맞춤형 가족 버킷리스트 생성 시작" }
        
        val prompt = buildPersonalizedTodoPrompt(familyContext, count)
        
        // Temporary hardcoded personalized todos
        return getPersonalizedHardcodedTodos(count)
    }
    
    /**
     * Todo 생성 프롬프트 구성
     */
    private fun buildTodoGenerationPrompt(category: TodoCategory, count: Int): String {
        return """
        당신은 가족을 위한 버킷리스트 전문가입니다.
        
        주제: ${category.description} 관련 가족 버킷리스트
        생성 개수: ${count}개
        
        다음 조건을 만족하는 버킷리스트를 생성해주세요:
        
        1. 가족 구성원 모두가 함께 할 수 있는 활동
        2. 실현 가능하고 구체적인 내용
        3. 의미 있고 추억을 만들 수 있는 활동
        4. 다양한 연령대가 즐길 수 있는 내용
        5. 한국 문화와 환경에 적합한 활동
        
        ${getCategorySpecificGuidelines(category)}
        
        응답 형식:
        - 각 버킷리스트 항목을 한 줄로 작성
        - 번호나 불필요한 설명 없이 활동 내용만 명시
        - 각 항목은 30자 이내로 간결하게 작성
        
        예시:
        가족과 함께 제주도 올레길 걷기
        할머니께 요리 배우며 가족 레시피 만들기
        가족 모두가 참여하는 텃밭 가꾸기
        """.trimIndent()
    }
    
    /**
     * 맞춤형 Todo 생성 프롬프트 구성
     */
    private fun buildPersonalizedTodoPrompt(familyContext: String, count: Int): String {
        return """
        당신은 가족을 위한 버킷리스트 전문가입니다.
        
        가족 정보: $familyContext
        생성 개수: ${count}개
        
        위 가족의 특성을 고려하여 맞춤형 버킷리스트를 생성해주세요:
        
        1. 가족 구성원의 특성과 관심사를 반영
        2. 실현 가능하고 구체적인 내용
        3. 가족 간의 유대감을 강화할 수 있는 활동
        4. 다양한 카테고리(여행, 활동, 경험, 취미, 학습, 유대감)를 균형있게 포함
        
        응답 형식:
        - 각 버킷리스트 항목을 한 줄로 작성
        - 번호나 불필요한 설명 없이 활동 내용만 명시
        - 각 항목은 30자 이내로 간결하게 작성
        """.trimIndent()
    }
    
    /**
     * 카테고리별 세부 가이드라인
     */
    private fun getCategorySpecificGuidelines(category: TodoCategory): String {
        return when (category) {
            TodoCategory.TRAVEL -> """
            여행 관련 세부 조건:
            - 국내외 다양한 여행지 포함
            - 당일치기부터 장기 여행까지 다양한 기간
            - 자연, 문화, 역사 등 다양한 테마
            - 가족의 취향과 예산을 고려한 현실적인 계획
            """.trimIndent()
            
            TodoCategory.ACTIVITY -> """
            활동 관련 세부 조건:
            - 실내외 다양한 활동 포함
            - 신체 활동과 정적인 활동의 균형
            - 계절별로 즐길 수 있는 활동
            - 모든 연령대가 참여 가능한 수준
            """.trimIndent()
            
            TodoCategory.EXPERIENCE -> """
            경험 관련 세부 조건:
            - 새롭고 특별한 경험 중심
            - 학습과 성장을 동반하는 활동
            - 가족만의 특별한 추억 만들기
            - 문화적, 교육적 가치가 있는 경험
            """.trimIndent()
            
            TodoCategory.HOBBY -> """
            취미 관련 세부 조건:
            - 지속 가능한 취미 활동
            - 창의성과 표현력을 기를 수 있는 활동
            - 개인적 성취와 가족 공유가 가능한 취미
            - 비용 효율적이고 접근성이 좋은 취미
            """.trimIndent()
            
            TodoCategory.LEARNING -> """
            학습 관련 세부 조건:
            - 가족이 함께 배우고 성장할 수 있는 주제
            - 실생활에 도움이 되는 실용적 학습
            - 재미있고 흥미로운 학습 방법
            - 각자의 강점을 활용한 상호 학습
            """.trimIndent()
            
            TodoCategory.BONDING -> """
            유대감 관련 세부 조건:
            - 가족 간의 소통과 이해를 증진하는 활동
            - 정서적 교감과 공감대 형성
            - 가족의 역사와 전통을 이어가는 활동
            - 서로에 대한 관심과 배려를 나타내는 활동
            """.trimIndent()
        }
    }
    
    /**
     * 하드코딩된 Todo 목록 (임시)
     */
    private fun getHardcodedTodos(category: TodoCategory, count: Int): List<String> {
        val todoMap = mapOf(
            TodoCategory.TRAVEL to listOf(
                "가족과 함께 제주도 올레길 걷기",
                "부산 해운대에서 일출 보기",
                "경주 불국사 역사 탐방하기",
                "강원도 정선 레일바이크 타기",
                "전주 한옥마을에서 하룻밤 보내기"
            ),
            TodoCategory.ACTIVITY to listOf(
                "가족 캠핑으로 자연 속에서 하룻밤",
                "함께 요리 클래스 수강하기",
                "가족 보드게임 토너먼트 개최",
                "텃밭 가꾸며 채소 기르기",
                "가족 사진 촬영하러 스튜디오 가기"
            ),
            TodoCategory.EXPERIENCE to listOf(
                "도자기 만들기 체험하기",
                "전통 차 만들기 배우기",
                "가족 뮤지컬 관람하기",
                "박물관에서 문화 체험하기",
                "천체 관측소에서 별 보기"
            ),
            TodoCategory.HOBBY to listOf(
                "가족 밴드 결성해서 연주하기",
                "함께 그림 그리기 취미 갖기",
                "가족 독서 모임 만들기",
                "정원 가꾸기 취미 시작하기",
                "가족 영상 제작해보기"
            ),
            TodoCategory.LEARNING to listOf(
                "새로운 언어 함께 배우기",
                "코딩 기초 함께 공부하기",
                "악기 연주법 배우기",
                "역사 공부하며 유적지 탐방",
                "요리 레시피 개발하고 공유하기"
            ),
            TodoCategory.BONDING to listOf(
                "가족 회의로 소통 시간 갖기",
                "어릴 적 추억 나누는 시간",
                "가족 전통 만들어 가기",
                "서로에게 편지 쓰기",
                "가족 운동회 개최하기"
            )
        )
        
        return todoMap[category]?.shuffled()?.take(count) ?: listOf("가족과 함께 즐거운 시간 보내기")
    }
    
    private fun getPersonalizedHardcodedTodos(count: Int): List<String> {
        val allTodos = listOf(
            "가족과 함께 제주도 여행하기",
            "할머니께 요리 배우며 가족 레시피 만들기",
            "가족 모두가 참여하는 텃밭 가꾸기",
            "매월 가족 영화의 밤 갖기",
            "가족 사진첩 만들어 추억 정리하기",
            "함께 새로운 취미 시작하기",
            "가족 캠핑으로 자연과 함께하기",
            "전통 시장에서 장보기 체험",
            "가족 운동 루틴 만들어 실천하기",
            "서로에게 감사 편지 쓰기"
        )
        
        return allTodos.shuffled().take(count)
    }
}