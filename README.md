# ☕ MOMENTO - CHALLKATHON 2025 Backend

> Spring Boot + Kotlin 기반 고성능 백엔드 API 서버

## 📋 목차

1. [프로젝트 개요](#-프로젝트-개요)
2. [프로젝트 구조](#-프로젝트-구조)
3. [코드 컨벤션](#-코드-컨벤션)
4. [커밋 컨벤션](#-커밋-컨벤션)
5. [개발 환경 설정](#-개발-환경-설정)
6. [품질 관리](#-품질-관리)
7. [API 문서화](#-api-문서화)
8. [협업 가이드](#-협업-가이드)

---

## 🎯 프로젝트 개요

### 기술 스택

- **Framework**: Spring Boot 3.5.3
- **Language**: Kotlin 1.9.25
- **Java**: 17
- **Database**: MySQL 8.0+
- **Security**: Spring Security + OAuth2 + JWT
- **Build Tool**: Gradle 8.x (Kotlin DSL)
- **Documentation**: Swagger/OpenAPI 3.0

### 주요 특징

- **DDD (Domain Driven Design)** 아키텍처 적용
- **모듈형 패키지 구조**로 확장성 보장
- **OAuth2 통합 인증** (소셜 로그인 지원)
- **JWT 기반 토큰 인증** 시스템
- **글로벌 예외 처리** 및 표준화된 API 응답

---

## 🏗️ 프로젝트 구조

### 디렉터리 구조

```
src/main/kotlin/com/challkathon/caffeine/
├── CaffeineApplication.kt                     # 메인 애플리케이션
├── auth/                                      # 🔐 인증/인가 모듈
│   ├── controller/                           # 인증 REST API
│   ├── dto/                                  # 인증 요청/응답 DTO
│   ├── enums/                                # 인증 관련 열거형
│   ├── exception/                            # 인증 예외 클래스
│   ├── filter/                               # JWT 필터
│   ├── handler/                              # OAuth2/보안 핸들러
│   ├── provider/                             # JWT 토큰 제공자
│   ├── security/                             # Spring Security 설정
│   ├── service/                              # 인증 비즈니스 로직
│   └── util/                                 # 인증 유틸리티
├── domain/                                   # 🎯 도메인 계층 (DDD Core)
│   ├── user/                                 # 사용자 도메인
│   │   ├── entity/                          # 사용자 엔티티 및 열거형
│   │   ├── repository/                      # 사용자 리포지토리 인터페이스
│   │   └── service/                         # 사용자 도메인 서비스
│   └── [other-domains]/                     # 기타 도메인 모듈
├── global/                                   # 🌐 글로벌 설정 및 공통 요소
│   ├── common/                              # 공통 기본 클래스
│   │   ├── BaseEntity.kt                    # JPA Auditing 기본 엔티티
│   │   └── BaseResponse.kt                  # 표준 API 응답 구조
│   ├── config/                              # 설정 클래스들
│   │   ├── SecurityConfig.kt                # Spring Security 설정
│   │   ├── JpaConfig.kt                     # JPA/Database 설정
│   │   ├── SwaggerConfig.kt                 # API 문서화 설정
│   │   └── WebConfig.kt                     # CORS 및 웹 설정
│   ├── exception/                           # 전역 예외 처리
│   │   ├── GlobalExceptionHandler.kt        # 글로벌 예외 핸들러
│   │   ├── CustomException.kt               # 커스텀 예외 클래스
│   │   └── ErrorCode.kt                     # 에러 코드 정의
│   ├── utils/                               # 유틸리티 클래스
│   └── validation/                          # 커스텀 검증 클래스
└── infrastructure/                          # 🔧 인프라스트럭처 계층
    ├── persistence/                         # 데이터 접근 구현체
    └── external/                            # 외부 API 연동
```

### 아키텍처 원칙

#### 1. 레이어드 아키텍처 + DDD 하이브리드

```
┌─────────────────────────────────────┐
│         Presentation Layer          │  ← Controller (REST API)
├─────────────────────────────────────┤
│         Application Layer           │  ← Service (비즈니스 로직)
├─────────────────────────────────────┤
│           Domain Layer              │  ← Entity, Repository Interface
├─────────────────────────────────────┤
│        Infrastructure Layer        │  ← Repository Implementation
└─────────────────────────────────────┘
```

#### 2. 모듈별 책임 분리

- **`auth/`**: 인증, 인가, 보안 관련 모든 로직
- **`domain/`**: 핵심 비즈니스 로직 및 도메인 모델
- **`global/`**: 전역 설정, 공통 컴포넌트, 횡단 관심사
- **`infrastructure/`**: 외부 시스템 연동 및 데이터 접근

#### 3. 의존성 규칙

```
auth/ ────────────┐
                 ├──→ domain/ ←──── infrastructure/
global/ ─────────┘
```

- `domain/`는 다른 계층에 의존하지 않음
- `auth/`, `global/`은 `domain/`에만 의존
- `infrastructure/`는 `domain/` 인터페이스를 구현

---

## 📝 코드 컨벤션

### Kotlin 코딩 스타일

#### 1. 클래스 및 인터페이스

```kotlin
// ✅ Good
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER
) : BaseEntity() {
    
    companion object {
        fun createLocalUser(email: String, name: String): User {
            return User(email = email, name = name)
        }
    }
    
    fun updateLastLogin() {
        // 비즈니스 로직
    }
}

// ❌ Bad
@Entity class User(val email:String,val name:String):BaseEntity()
```

#### 2. 함수 및 변수명

```kotlin
// ✅ Good - 명확하고 의미있는 이름
fun findUserByEmailAndStatus(email: String, status: UserStatus): User?
val isEmailVerified: Boolean = user.emailVerifiedAt != null
private val tokenExpirationTime: Duration = Duration.ofHours(24)

// ❌ Bad - 축약되거나 모호한 이름
fun findUsrByEmailAndStat(email: String, stat: UserStatus): User?
val isVerified: Boolean = user.emailVerifiedAt != null
private val expTime: Duration = Duration.ofHours(24)
```

#### 3. 널 안전성

```kotlin
// ✅ Good - 명시적 널 처리
fun processUser(userId: String?): UserResponse {
    return userId?.let { id ->
        userRepository.findById(id)?.let { user ->
            UserResponse.from(user)
        } ?: throw UserNotFoundException("User not found: $id")
    } ?: throw IllegalArgumentException("User ID cannot be null")
}

// ❌ Bad - 암묵적 널 처리
fun processUser(userId: String?): UserResponse {
    val user = userRepository.findById(userId!!)!!
    return UserResponse.from(user)
}
```

#### 4. 데이터 클래스 및 DTO

```kotlin
// ✅ Good
data class CreateUserRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,
    
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다")
    val name: String,
    
    @field:Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", 
                  message = "비밀번호는 8자 이상, 영문자와 숫자를 포함해야 합니다")
    val password: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: Role,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                name = user.name,
                role = user.role,
                createdAt = user.createdAt!!,
                lastLoginAt = user.lastLoginAt
            )
        }
    }
}
```

### Spring Boot 컨벤션

#### 1. 컨트롤러

```kotlin
// ✅ Good
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회", description = "ID로 사용자 정보를 조회합니다")
    fun getUser(
        @PathVariable id: String
    ): ResponseEntity<BaseResponse<UserResponse>> {
        val user = userService.findById(id)
        return ResponseEntity.ok(BaseResponse.success(user))
    }
    
    @PostMapping
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다")
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<BaseResponse<UserResponse>> {
        val user = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success(user))
    }
}
```

#### 2. 서비스

```kotlin
// ✅ Good
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val log = KotlinLogging.logger {}
    
    @Transactional
    fun create(request: CreateUserRequest): UserResponse {
        log.info { "Creating user with email: ${request.email}" }
        
        // 중복 검증
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException("Email already exists: ${request.email}")
        }
        
        // 엔티티 생성
        val user = User.createLocalUser(
            email = request.email,
            name = request.name,
            password = passwordEncoder.encode(request.password)
        )
        
        // 저장 및 응답
        val savedUser = userRepository.save(user)
        log.info { "User created successfully: ${savedUser.id}" }
        
        return UserResponse.from(savedUser)
    }
    
    fun findById(id: String): UserResponse {
        val user = userRepository.findById(id)
            ?: throw UserNotFoundException("User not found: $id")
        
        return UserResponse.from(user)
    }
}
```

#### 3. 리포지토리

```kotlin
// ✅ Good
@Repository
interface UserRepository : JpaRepository<User, String> {
    
    fun findByEmail(email: String): User?
    
    fun existsByEmail(email: String): Boolean
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= :since")
    fun findRecentUsersByRole(
        @Param("role") role: Role,
        @Param("since") since: LocalDateTime
    ): List<User>
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginAt WHERE u.id = :id")
    fun updateLastLoginAt(
        @Param("id") id: String,
        @Param("loginAt") loginAt: LocalDateTime
    )
}
```

### 로깅 컨벤션

```kotlin
// ✅ Good - 구조화된 로깅
private val log = KotlinLogging.logger {}

class UserService {
    fun processUser(userId: String) {
        log.info { "Processing user: userId=$userId" }
        
        try {
            // 비즈니스 로직
            log.debug { "User processing completed: userId=$userId" }
        } catch (e: Exception) {
            log.error(e) { "Failed to process user: userId=$userId" }
            throw e
        }
    }
}

// ❌ Bad - 문자열 연결 로깅
private val log = LoggerFactory.getLogger(UserService::class.java)

class UserService {
    fun processUser(userId: String) {
        log.info("Processing user: " + userId)  // 성능 이슈
        
        try {
            // 비즈니스 로직
        } catch (e: Exception) {
            log.error("Error: " + e.message)  // 스택 트레이스 누락
        }
    }
}
```

---

## 🔄 커밋 컨벤션

### Conventional Commits 기반 메시지 형식

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### 1. 커밋 타입

| Type       | 설명                | 예시                                      |
|------------|-------------------|-----------------------------------------|
| `feat`     | 새로운 기능 추가         | `feat(auth): JWT 토큰 인증 구현`              |
| `fix`      | 버그 수정             | `fix(user): 이메일 중복 검증 오류 수정`            |
| `docs`     | 문서 변경             | `docs: API 문서 업데이트`                     |
| `style`    | 코드 포맷팅, 세미콜론 추가 등 | `style: 코틀린 코드 스타일 적용`                  |
| `refactor` | 기능 변경 없는 코드 리팩터링  | `refactor(service): UserService 메서드 분리` |
| `test`     | 테스트 코드 추가/수정      | `test(auth): JWT 토큰 검증 테스트 추가`          |
| `chore`    | 빌드 프로세스, 도구 설정 변경 | `chore: Gradle 의존성 업데이트`                |
| `perf`     | 성능 개선             | `perf(db): 사용자 조회 쿼리 최적화`               |
| `ci`       | CI/CD 설정 변경       | `ci: GitHub Actions 워크플로우 추가`           |
| `build`    | 빌드 시스템 변경         | `build: Dockerfile 최적화`                 |

#### 2. 스코프 (선택사항)

```
feat(auth): OAuth2 로그인 구현
fix(user): 프로필 업데이트 버그 수정  
refactor(global): 예외 처리 구조 개선
test(domain): User 엔티티 테스트 추가
```

#### 3. 커밋 메시지 작성 규칙

**✅ Good Examples:**

```bash
feat(auth): JWT 토큰 기반 인증 시스템 구현

- JwtTokenProvider 클래스 추가
- 토큰 생성, 검증, 파싱 로직 구현
- Spring Security 필터 체인 연동

Resolves: #123

fix(user): 이메일 중복 검증 로직 수정

중복 검증 시 대소문자 구분하지 않도록 수정

refactor(global): BaseResponse 제네릭 구조 개선

타입 안전성 향상 및 코드 중복 제거

test(auth): AuthService 단위 테스트 추가

- 로그인 성공/실패 시나리오 테스트
- JWT 토큰 생성 검증 테스트
- 예외 상황 처리 테스트
```

**❌ Bad Examples:**

```bash
update user service           # 타입 누락, 설명 부족
Fix bug                      # 대문자 시작, 구체성 부족
feat: add new feature        # 기능에 대한 구체적 설명 없음
🎉 새로운 기능 추가            # 이모지 사용 금지
```

### 브랜치 전략

#### Git Flow 기반 브랜치 관리

```
main (production)
├── develop (development)
│   ├── feature/auth-jwt-implementation
│   ├── feature/user-profile-management  
│   └── feature/oauth2-social-login
├── release/v1.0.0
└── hotfix/critical-security-patch
```

#### 브랜치 네이밍 규칙

```bash
# 기능 개발
feature/기능명-구체적설명
feature/auth-jwt-token
feature/user-profile-crud
feature/oauth2-kakao-login

# 버그 수정
bugfix/이슈번호-간단설명
bugfix/123-email-validation
bugfix/user-duplicate-check

# 핫픽스
hotfix/심각도-간단설명  
hotfix/critical-security-patch
hotfix/database-connection-fix

# 릴리즈
release/버전번호
release/v1.0.0
release/v1.1.0
```

---

## 🛠️ 개발 환경 설정

### 필수 요구사항

- **Java**: OpenJDK 17+
- **Kotlin**: 1.9.25+
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA (권장)

### 로컬 개발 환경 구축

#### 1. 프로젝트 클론 및 빌드

```bash
# 프로젝트 클론
git clone <repository-url>
cd caffeine

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

#### 2. 데이터베이스 설정

```sql
-- MySQL 데이터베이스 생성
CREATE DATABASE caffeine_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'caffeine'@'localhost' IDENTIFIED BY 'caffeine123!';
GRANT ALL PRIVILEGES ON caffeine_local.* TO 'caffeine'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. 환경 설정 파일

**`src/main/resources/application.yml`** (공통 설정)

```yaml
spring:
  profiles:
    active: local
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    show-sql: false

jwt:
  secret-key: ${JWT_SECRET_KEY:your-256-bit-secret}
  access-token-expiration: 3600000  # 1시간
  refresh-token-expiration: 1209600000  # 2주

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**`src/main/resources/application-local.yml`** (로컬 개발)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/caffeine_local?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: caffeine
    password: caffeine123!
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: ${KAKAO_CLIENT_ID:your-kakao-client-id}
            client-secret: ${KAKAO_CLIENT_SECRET:your-kakao-client-secret}
            scope: profile_nickname,account_email
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/kakao
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

logging:
  level:
    com.challkathon.momento: debug
    org.springframework.security: debug
    org.hibernate.SQL: debug
    org.hibernate.type.descriptor.sql.BasicBinder: trace
```

#### 4. IDE 설정 (IntelliJ IDEA)

**Kotlin 코드 스타일 설정:**

```
File → Settings → Editor → Code Style → Kotlin
- Indentation: 4 spaces
- Continuation indent: 8 spaces
- Use tab character: 체크 해제
- Import optimization: 자동 import 정리 활성화
```

**Live Templates 추가:**

```kotlin
// Kotlin Class Template
class $CLASS_NAME$ {
    
}

// Kotlin Data Class Template  
data class $CLASS_NAME$(
    val $PROPERTY$: $TYPE$
)

// Service Template
@Service
@Transactional(readOnly = true)
class $SERVICE_NAME$(
    private val $REPOSITORY$: $REPOSITORY_TYPE$
) {
    
    private val log = KotlinLogging.logger {}
    
}
```

### Docker를 이용한 개발 환경

**`docker-compose.yml`**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: caffeine-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123!
      MYSQL_DATABASE: caffeine_local
      MYSQL_USER: caffeine
      MYSQL_PASSWORD: caffeine123!
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    container_name: caffeine-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

```bash
# Docker 환경 실행
docker-compose up -d

# 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 🔍 품질 관리

### 테스트 전략

#### 1. 테스트 피라미드

```
    🔺 E2E Tests (5%)
      Integration Tests (15%)  
        Unit Tests (80%)
```

#### 2. 테스트 커버리지 목표

- **전체**: 80% 이상
- **도메인 계층**: 90% 이상
- **서비스 계층**: 85% 이상
- **컨트롤러 계층**: 70% 이상

#### 3. 테스트 작성 가이드

**단위 테스트 (Unit Test)**

```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest {
    
    @MockK
    private lateinit var userRepository: UserRepository
    
    @MockK
    private lateinit var passwordEncoder: PasswordEncoder
    
    @InjectMockKs
    private lateinit var userService: UserService
    
    @Test
    fun `사용자 생성 성공`() {
        // Given
        val request = CreateUserRequest(
            email = "test@example.com",
            name = "테스트 사용자",
            password = "password123"
        )
        val encodedPassword = "encoded-password"
        val savedUser = User.createLocalUser(
            email = request.email,
            name = request.name,
            password = encodedPassword
        ).apply { id = "user-id" }
        
        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userRepository.save(any()) } returns savedUser
        
        // When
        val result = userService.create(request)
        
        // Then
        result.email shouldBe request.email
        result.name shouldBe request.name
        verify { userRepository.existsByEmail(request.email) }
        verify { userRepository.save(any()) }
    }
    
    @Test
    fun `이메일 중복시 예외 발생`() {
        // Given
        val request = CreateUserRequest(
            email = "duplicate@example.com",
            name = "테스트 사용자",
            password = "password123"
        )
        
        every { userRepository.existsByEmail(request.email) } returns true
        
        // When & Then
        shouldThrow<DuplicateEmailException> {
            userService.create(request)
        }
    }
}
```

**통합 테스트 (Integration Test)**

```kotlin
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserIntegrationTest(
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    
    @Test
    @Transactional
    @Rollback
    fun `사용자 생성부터 조회까지 전체 플로우 테스트`() {
        // Given
        val createRequest = CreateUserRequest(
            email = "integration@test.com",
            name = "통합테스트",
            password = "test123!"
        )
        
        // When - 사용자 생성
        val createdUser = userService.create(createRequest)
        
        // Then - 생성 검증
        createdUser.email shouldBe createRequest.email
        createdUser.name shouldBe createRequest.name
        
        // When - 사용자 조회
        val foundUser = userService.findById(createdUser.id)
        
        // Then - 조회 검증
        foundUser.id shouldBe createdUser.id
        foundUser.email shouldBe createdUser.email
        
        // When - 데이터베이스 직접 확인
        val userEntity = userRepository.findById(createdUser.id)
        
        // Then - 데이터베이스 검증
        userEntity shouldNotBe null
        userEntity!!.email shouldBe createRequest.email
    }
}
```

**API 테스트 (Controller Test)**

```kotlin
@WebMvcTest(UserController::class)
@Import(SecurityConfig::class)
class UserControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockK
    private lateinit var userService: UserService
    
    @Test
    @WithMockUser
    fun `사용자 생성 API 테스트`() {
        // Given
        val request = CreateUserRequest(
            email = "api@test.com",
            name = "API테스트",
            password = "apitest123!"
        )
        val response = UserResponse(
            id = "user-id",
            email = request.email,
            name = request.name,
            role = Role.USER,
            createdAt = LocalDateTime.now(),
            lastLoginAt = null
        )
        
        every { userService.create(request) } returns response
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value(request.email))
        .andExpect(jsonPath("$.data.name").value(request.name))
        .andDo(print())
    }
    
    @Test
    @WithMockUser  
    fun `잘못된 이메일 형식으로 사용자 생성시 400 에러`() {
        // Given
        val invalidRequest = CreateUserRequest(
            email = "invalid-email",
            name = "테스트",
            password = "test123!"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
        .andExpected(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"))
    }
}
```

### 테스트 실행 명령어

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "UserServiceTest"

# 특정 테스트 메서드 실행  
./gradlew test --tests "UserServiceTest.사용자 생성 성공"

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# 커버리지 검증
./gradlew jacocoTestCoverageVerification
```

### 코드 품질 도구

#### 1. 정적 분석 도구 설정

**`build.gradle.kts`에 추가:**

```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
    jacoco
}

// Ktlint 설정
ktlint {
    version.set("0.50.0")
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

// Detekt 설정
detekt {
    config = files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

// JaCoCo 설정
jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

#### 2. 코드 품질 검사 명령어

```bash
# 코드 포맷팅 검사
./gradlew ktlintCheck

# 코드 포맷팅 자동 수정
./gradlew ktlintFormat

# 정적 분석 실행
./gradlew detekt

# 전체 품질 검사
./gradlew check
```

---

## 📚 API 문서화

### Swagger UI 설정

#### 1. 의존성 설정 (`build.gradle.kts`)

```kotlin
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
}
```

#### 2. Swagger 설정 클래스

```kotlin
@Configuration
@EnableWebSecurity
class SwaggerConfig {
    
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("CAFFEINE API")
                    .description("CHALLKATHON 2025 백엔드 API 문서")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("CAFFEINE Team")
                            .email("team@caffeine.com")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버"),
                    Server()
                        .url("https://api-dev.caffeine.com")
                        .description("개발 서버"),
                    Server()
                        .url("https://api.caffeine.com")
                        .description("운영 서버")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 토큰을 입력하세요")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("bearerAuth")
            )
    }
}
```

#### 3. API 문서화 어노테이션 활용

```kotlin
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {
    
    @PostMapping
    @Operation(
        summary = "사용자 생성",
        description = "새로운 사용자 계정을 생성합니다.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "이메일 중복",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    fun createUser(
        @Parameter(description = "사용자 생성 요청 데이터", required = true)
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<BaseResponse<UserResponse>> {
        val user = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success(user))
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "사용자 조회",
        description = "사용자 ID로 사용자 정보를 조회합니다."
    )
    fun getUser(
        @Parameter(description = "사용자 ID", required = true, example = "user-123")
        @PathVariable id: String
    ): ResponseEntity<BaseResponse<UserResponse>> {
        val user = userService.findById(id)
        return ResponseEntity.ok(BaseResponse.success(user))
    }
}
```

### API 문서 접근

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

---

## 🤝 협업 가이드

### 코드 리뷰 체크리스트

#### 📋 기본 체크사항

- [ ] **빌드 성공**: 로컬에서 `./gradlew build` 성공 확인
- [ ] **테스트 통과**: 모든 기존 테스트 통과 및 신규 테스트 작성
- [ ] **코드 스타일**: Ktlint, Detekt 규칙 준수
- [ ] **커밋 메시지**: Conventional Commits 형식 준수

#### 🔍 코드 품질 체크사항

- [ ] **단일 책임 원칙**: 클래스/함수가 하나의 책임만 담당
- [ ] **의존성 주입**: 생성자 주입 방식 사용
- [ ] **예외 처리**: 적절한 예외 처리 및 로깅
- [ ] **널 안전성**: 코틀린 널 안전성 기능 활용
- [ ] **불변성**: `val` 키워드 우선 사용
- [ ] **의미있는 이름**: 변수, 함수, 클래스명이 의도를 명확히 표현

#### 🏗️ 아키텍처 체크사항

- [ ] **계층 분리**: 각 계층의 책임이 명확히 구분
- [ ] **의존성 방향**: 상위 계층이 하위 계층에만 의존
- [ ] **도메인 중심**: 비즈니스 로직이 도메인 계층에 집중
- [ ] **API 일관성**: RESTful API 설계 원칙 준수

#### 🔒 보안 체크사항

- [ ] **인증/인가**: 적절한 권한 검증 로직
- [ ] **입력 검증**: 사용자 입력 데이터 검증
- [ ] **민감정보**: 로그나 응답에 민감정보 노출 방지
- [ ] **SQL 인젝션**: 파라미터 바인딩 사용

### Pull Request 템플릿

```markdown
## 📝 변경 사항
<!-- 이번 PR에서 변경된 내용을 간략히 설명해주세요 -->

## 🎯 관련 이슈
<!-- 관련된 이슈 번호를 입력해주세요 -->
- Closes #이슈번호

## 🧪 테스트
<!-- 어떤 테스트를 추가했는지, 어떻게 검증했는지 설명해주세요 -->
- [ ] 단위 테스트 추가
- [ ] 통합 테스트 추가  
- [ ] 수동 테스트 완료

## 📸 스크린샷 (선택사항)
<!-- API 변경사항이나 UI 관련 변경이 있다면 스크린샷을 첨부해주세요 -->

## ✅ 체크리스트
- [ ] 로컬에서 빌드 및 테스트 성공
- [ ] 코드 스타일 검사 통과 (`./gradlew ktlintCheck`)
- [ ] API 문서 업데이트 (필요한 경우)
- [ ] 마이그레이션 스크립트 추가 (DB 변경이 있는 경우)

## 💬 추가 정보
<!-- 리뷰어가 알아야 할 추가 정보가 있다면 작성해주세요 -->
```

### 이슈 템플릿

#### 🐛 버그 리포트

```markdown
## 🐛 버그 설명
<!-- 발생한 버그에 대해 명확하고 간결하게 설명해주세요 -->

## 🔄 재현 단계
1. '...' 페이지로 이동
2. '...' 버튼 클릭
3. '...' 입력
4. 에러 발생

## 🎯 예상 동작
<!-- 정상적으로 동작해야 하는 방식을 설명해주세요 -->

## 💥 실제 동작
<!-- 실제로 발생한 동작을 설명해주세요 -->

## 🖼️ 스크린샷
<!-- 가능하다면 스크린샷을 첨부해주세요 -->

## 🌍 환경
- OS: [예: macOS 13.0]
- Browser: [예: Chrome 118.0]
- Version: [예: v1.0.0]

## 📋 추가 정보
<!-- 기타 추가적인 정보나 컨텍스트가 있다면 작성해주세요 -->
```

#### ✨ 기능 요청

```markdown
## 🎯 기능 설명
<!-- 원하는 기능에 대해 명확하고 간결하게 설명해주세요 -->

## 💪 동기
<!-- 이 기능이 왜 필요한지 설명해주세요 -->

## 📝 상세 설명
<!-- 기능의 동작 방식을 자세히 설명해주세요 -->

## 🎨 UI/UX 제안 (선택사항)
<!-- UI가 관련된 기능이라면 와이어프레임이나 스크린샷을 첨부해주세요 -->

## 📚 추가 컨텍스트
<!-- 기타 추가적인 정보나 참고자료가 있다면 작성해주세요 -->
```

### 팀 커뮤니케이션 가이드

#### 📅 정기 미팅

- **데일리 스탠드업**: 매일 오전 10시 (15분)
    - 어제 한 일
    - 오늘 할 일
    - 블로커나 도움이 필요한 부분
- **스프린트 계획**: 매주 월요일 (1시간)
- **회고**: 매주 금요일 (30분)

#### 💬 커뮤니케이션 채널

- **Slack**: 일상적인 소통
- **GitHub**: 코드 리뷰, 이슈 트래킹
- **Notion**: 문서화, 회의록
- **Figma**: UI/UX 디자인 리뷰

#### 🚨 긴급 상황 대응

1. **즉시 알림**: Slack `@channel` 멘션
2. **이슈 생성**: GitHub에서 `bug` 라벨로 이슈 생성
3. **핫픽스 브랜치**: `hotfix/` 브랜치로 빠른 수정
4. **팀 공유**: 수정 완료 후 전체 팀에 공유

---

## 🚀 배포 가이드

### 환경별 설정

#### 개발 환경 (Development)

```yaml
# application-dev.yml
spring:
  profiles:
    include: oauth
  datasource:
    url: jdbc:mysql://dev-db:3306/caffeine_dev
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

logging:
  level:
    com.challkathon.momento: info
    org.springframework.security: warn
```

#### 운영 환경 (Production)

```yaml
# application-prod.yml
spring:
  profiles:
    include: oauth
  datasource:
    url: jdbc:mysql://prod-db:3306/caffeine_prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

jwt:
  secret-key: ${JWT_SECRET_KEY}

logging:
  level:
    com.challkathon.momento: warn
    org.springframework.security: error
```

### Docker 배포

**`Dockerfile`**

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/caffeine-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**배포 명령어**

```bash
# 빌드
./gradlew bootJar

# Docker 이미지 생성
docker build -t caffeine:latest .

# 컨테이너 실행
docker run -d -p 8080:8080 --name caffeine-app caffeine:latest
```

---

## 📖 참고 자료

### 공식 문서

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)

### 코딩 가이드

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Spring Boot Best Practices](https://springframework.guru/spring-boot-best-practices/)

### 도구

- [Ktlint](https://ktlint.github.io/)
- [Detekt](https://detekt.dev/)
- [JaCoCo](https://www.jacoco.org/jacoco/)

---

## 📞 문의 및 지원

프로젝트 관련 문의사항이나 도움이 필요한 경우:

- **GitHub Issues**: 버그 리포트 및 기능 요청
- **팀 Slack**: `#caffeine-backend` 채널
- **이메일**: team@caffeine.com

---

<div align="center">

**🎯 함께 만들어가는 고품질 백엔드 서비스**

Made with ❤️ by CAFFEINE Team

</div>
