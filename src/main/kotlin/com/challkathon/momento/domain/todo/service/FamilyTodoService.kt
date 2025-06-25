package com.challkathon.momento.domain.todo.service

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.todo.TodoList
import com.challkathon.momento.domain.todo.dto.response.FamilyTodoResponse
import com.challkathon.momento.domain.todo.dto.response.RecentTodoResponse
import com.challkathon.momento.domain.todo.exception.TodoException
import com.challkathon.momento.domain.todo.exception.code.TodoErrorStatus
import com.challkathon.momento.domain.todo.mapping.FamilyTodoList
import com.challkathon.momento.domain.todo.repository.FamilyTodoListRepository
import com.challkathon.momento.domain.todo.repository.TodoListRepository
import com.challkathon.momento.domain.user.repository.UserRepository
import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.global.infrastructure.AmazonS3Manager
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
@Transactional
class FamilyTodoService(
    private val familyTodoListRepository: FamilyTodoListRepository,
    private val todoListRepository: TodoListRepository,
    private val familyRepository: FamilyRepository,
    private val userRepository: UserRepository,
    private val todoPoolService: TodoPoolService,
    private val todoGeneratorService: TodoGeneratorService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val amazonS3Manager: AmazonS3Manager
) {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        const val CACHE_KEY_PREFIX = "family:todos"
        const val TODO_COUNT_PER_GENERATION = 3  // 한 번에 3개씩 생성
        
        // 파일 업로드 제한
        const val MAX_FILE_SIZE = 50 * 1024 * 1024L  // 50MB
        val ALLOWED_IMAGE_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp")
    }
    
    /**
     * 가족용 버킷리스트 생성 (Redis 캐싱 + 3-tier 전략)
     */
    fun generateFamilyBucketList(userId: Long): List<FamilyTodoResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }
        
        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)
        
        logger.info { "가족 ${family.id}의 버킷리스트 생성 시작" }
        
        // 1차: 풀에서 조회
        val poolTodos = getTodosFromPool(TODO_COUNT_PER_GENERATION)
        if (poolTodos.isNotEmpty()) {
            logger.info { "풀에서 버킷리스트 조회 성공: ${poolTodos.size}개" }
            return createFamilyTodosFromPool(family, poolTodos).map { mapToFamilyTodoResponse(it) }
        }
        
        // 2차: AI 실시간 생성
        logger.info { "AI를 통한 실시간 버킷리스트 생성" }
        return generateAITodos(family).map { mapToFamilyTodoResponse(it) }
    }
    
    /**
     * 가족의 Todo 목록 조회
     */
    @Transactional(readOnly = true)
    fun getFamilyTodos(userId: Long): List<FamilyTodoResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }
        
        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)
        
        return familyTodoListRepository.findByFamilyOrderByAssignedAtDesc(family)
            .map { mapToFamilyTodoResponse(it) }
    }

    /**
     * 상태별 가족 Todo 목록 조회
     */
    @Transactional(readOnly = true)
    fun getFamilyTodosByStatus(userId: Long, status: String?): List<FamilyTodoResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }
        
        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)
        
        val familyTodos = when (status?.lowercase()) {
            "completed" -> familyTodoListRepository.findByFamilyAndStatusOrderByAssignedAtDesc(
                family, com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus.COMPLETED
            )
            "pending" -> familyTodoListRepository.findByFamilyAndStatusOrderByAssignedAtDesc(
                family, com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus.ASSIGNED
            )
            else -> familyTodoListRepository.findByFamilyOrderByAssignedAtDesc(family)
        }
        
        return familyTodos.map { mapToFamilyTodoResponse(it) }
    }
    
    /**
     * Todo 완료 처리 (인증샷과 메모 필수)
     */
    fun completeTodo(
        userId: Long, 
        todoId: Long, 
        proofImage: MultipartFile, 
        memo: String
    ): FamilyTodoResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }
        
        val familyTodo = familyTodoListRepository.findById(todoId)
            .orElseThrow { TodoException(TodoErrorStatus.TODO_NOT_FOUND) }
        
        // 가족 소속 확인
        if (user.family?.id != familyTodo.family.id) {
            throw TodoException(TodoErrorStatus.TODO_ACCESS_DENIED)
        }
        
        // 메모 필수 검증
        if (memo.isBlank()) {
            throw TodoException(TodoErrorStatus.TODO_COMPLETION_MEMO_REQUIRED)
        }
        
        // 인증샷 업로드 (필수)
        validateProofImage(proofImage)
        logger.info { "인증샷 업로드 시작: ${proofImage.originalFilename}, 크기: ${proofImage.size}bytes" }
        val imageUrl = amazonS3Manager.uploadFile(proofImage)
        logger.info { "인증샷 업로드 완료: $imageUrl" }
        
        // Todo 완료 처리
        familyTodo.complete()
        familyTodo.memo = memo
        familyTodo.imageUrl = imageUrl
        
        val savedTodo = familyTodoListRepository.save(familyTodo)
        return mapToFamilyTodoResponse(savedTodo)
    }
    
    // 생성 제한 및 캐시 관련 메서드 제거됨 - 무제한 생성 허용
    
    /**
     * 풀에서 Todo 조회
     */
    private fun getTodosFromPool(count: Int): List<String> {
        return try {
            todoPoolService.getMixedTodosFromPool(count)
        } catch (e: Exception) {
            logger.warn { "풀에서 Todo 조회 실패: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * 풀의 Todo로 FamilyTodoList 생성
     */
    private fun createFamilyTodosFromPool(family: Family, todoContents: List<String>): List<FamilyTodoList> {
        return todoContents.map { content ->
            val todoList = TodoList(
                content = content,
                isAIGenerated = false,
                generatedDate = LocalDate.now()
            )
            val savedTodo = todoListRepository.save(todoList)
            
            val familyTodo = FamilyTodoList(
                todoList = savedTodo,
                family = family,
                assignedAt = LocalDateTime.now()
            )
            familyTodoListRepository.save(familyTodo)
        }
    }
    
    /**
     * AI를 통한 Todo 생성
     */
    private fun generateAITodos(family: Family): List<FamilyTodoList> {
        val familyContext = buildFamilyContext(family)
        val aiTodoContents = todoGeneratorService.generatePersonalizedBucketList(
            familyContext, 
            TODO_COUNT_PER_GENERATION
        )
        
        return aiTodoContents.map { content ->
            val todoList = TodoList(
                content = content,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            )
            val savedTodo = todoListRepository.save(todoList)
            
            val familyTodo = FamilyTodoList(
                todoList = savedTodo,
                family = family,
                assignedAt = LocalDateTime.now()
            )
            familyTodoListRepository.save(familyTodo)
        }
    }
    
    /**
     * 가족 컨텍스트 구성
     */
    private fun buildFamilyContext(family: Family): String {
        val members = family.users
        val memberInfo = members.joinToString(", ") { member ->
            "${member.familyRole?.description ?: "가족"}: ${member.username}"
        }
        
        return """
        가족 구성원: $memberInfo
        가족 인원: ${family.count}명
        가족 생성일: ${family.createdAt.toLocalDate()}
        """.trimIndent()
    }
    
    // 캐싱 메서드 제거됨 - 매번 새로운 Todo 생성
    
    /**
     * 인증샷 이미지 유효성 검증
     */
    private fun validateProofImage(image: MultipartFile) {
        // 파일 크기 검증 (50MB)
        if (image.size > MAX_FILE_SIZE) {
            throw TodoException(TodoErrorStatus.TODO_IMAGE_SIZE_EXCEEDED)
        }
        
        // 파일 타입 검증
        val contentType = image.contentType
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.lowercase())) {
            throw TodoException(TodoErrorStatus.TODO_IMAGE_TYPE_NOT_SUPPORTED)
        }
        
        // 파일명 검증
        val filename = image.originalFilename
        if (filename.isNullOrBlank()) {
            throw TodoException(TodoErrorStatus.TODO_IMAGE_FILENAME_INVALID)
        }
        
        // 빈 파일 검증
        if (image.isEmpty) {
            throw TodoException(TodoErrorStatus.TODO_IMAGE_EMPTY)
        }
        
        logger.debug { "파일 유효성 검증 완료: $filename (${image.size}bytes, $contentType)" }
    }

    /**
     * 개별 Todo 조회
     */
    @Transactional(readOnly = true)
    fun getFamilyTodo(userId: Long, todoId: Long): FamilyTodoResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        val familyTodo = familyTodoListRepository.findById(todoId)
            .orElseThrow { TodoException(TodoErrorStatus.TODO_NOT_FOUND) }

        // 가족 소속 확인
        if (user.family?.id != familyTodo.family.id) {
            throw TodoException(TodoErrorStatus.TODO_ACCESS_DENIED)
        }

        return mapToFamilyTodoResponse(familyTodo)
    }

    /**
     * 최신 Todo 3개 조회
     */
    @Transactional(readOnly = true)
    fun getRecentTodos(userId: Long): List<RecentTodoResponse>? {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }

        val family = user.family
            ?: throw FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)

        val pageable = PageRequest.of(0, 3)
        val recentTodos = familyTodoListRepository.findRecentByFamily(family, pageable)

        return if (recentTodos.isEmpty()) {
            null
        } else {
            recentTodos.map { familyTodo ->
                RecentTodoResponse(
                    id = familyTodo.id,
                    content = familyTodo.todoList.content,
                    category = familyTodo.todoList.category,
                    status = familyTodo.status,
                    assignedAt = familyTodo.assignedAt,
                    isAIGenerated = familyTodo.todoList.isAIGenerated
                )
            }
        }
    }

    /**
     * FamilyTodoList를 FamilyTodoResponse로 변환
     */
    private fun mapToFamilyTodoResponse(familyTodo: FamilyTodoList): FamilyTodoResponse {
        return FamilyTodoResponse(
            familyTodoId = familyTodo.id,
            content = familyTodo.todoList.content,
            category = familyTodo.todoList.category.description,
            status = familyTodo.status,
            assignedAt = familyTodo.assignedAt,
            dueDate = familyTodo.dueDate,
            completedAt = familyTodo.completedAt,
            memo = familyTodo.memo,
            imageUrl = familyTodo.imageUrl
        )
    }
}