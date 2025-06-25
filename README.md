# 🎊 Momento - 2025 CHALLKATHON

> 추억을 기록하고 공유하는 소셜 플랫폼

## 📋 프로젝트 개요

**Momento**는 사용자들이 소중한 순간들을 기록하고, 가족과 추억을 공유할 수 있는 소셜 플랫폼입니다.

### 🎯 주요 기능

- 📸 **추억 기록**: 사진과 함께 특별한 순간들을 기록
- 👥 **소셜 공유**: 친구들과 추억을 공유하고 소통
- 🔐 **소셜 로그인**: 카카오 로그인을 통한 간편한 회원가입
- 🎨 **개인화**: 나만의 추억 컬렉션 구성

## 🏗️ 기술 스택

### Backend

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.3
- **Database**: MySQL 8.0
- **Security**: Spring Security + JWT + OAuth2
- **Build Tool**: Gradle 8.7

### Infrastructure

- **Docker**: 컨테이너 기반 배포
- **Nginx**: 리버스 프록시 + SSL
- **Let's Encrypt**: 자동 SSL 인증서
- **AWS EC2**: 배포 환경
- **GitHub Actions**: CI/CD

## 🚀 빠른 시작

### 로컬 개발 환경

1. **프로젝트 클론**

```bash
git clone https://github.com/CHALLKATHON-Official/2025_CHALLKATHON_CAFFEINEADDICT_BE.git
cd momento
```

2. **IntelliJ 환경변수 설정**
   Run Configuration에서 다음 환경변수 설정:

```
SPRING_PROFILES_ACTIVE=local
JWT_SECRET=your_jwt_secret_key
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

3. **MySQL 로컬 설정**

```sql
CREATE DATABASE momento_local;
```

4. **애플리케이션 실행**

```bash
./gradlew bootRun
```

### Docker 배포

1. **Docker Compose로 전체 스택 실행**

```bash
docker-compose up -d
```

2. **서비스 확인**

- **애플리케이션**: `https://your-domain.com`
- **헬스체크**: `https://your-domain.com/actuator/health`

## 📚 문서

### 개발 가이드
- 🚀 [API 사용 가이드](API_USAGE_GUIDE.md) - AI 질문 생성 API 완전 가이드
- ⚡ [API 빠른 참조](API_QUICK_REFERENCE.md) - API 치트시트
- 🤖 [AI 시스템 가이드](AI_QUESTION_SYSTEM_GUIDE.md) - AI 질문 시스템 상세
- 🏗️ [아키텍처 문서](ARCHITECTURE.md) - 시스템 아키텍처 설명
- 📊 [DB 스키마 - 질문 시스템](docs/DATABASE_SCHEMA_QUESTION.md) - 질문 관련 테이블 구조

### 운영 가이드
- 🚀 [배포 가이드](DEPLOYMENT.md) - Docker 기반 프로덕션 배포
- 🌱 [Spring Profile 가이드](SPRING_PROFILES_GUIDE.md) - 환경별 설정 관리
- 🔐 [GitHub Secrets 설정](GITHUB_SECRETS.md) - CI/CD 환경변수
- ⚙️ [IntelliJ 설정](INTELLIJ_SETUP.md) - 로컬 개발 환경

## 🔧 API 문서

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health`

## 🏗️ 프로젝트 구조

### 패키지 구조

```
src/main/kotlin/com/challkathon/momento/
├── MomentoApplication.kt                 # 메인 애플리케이션
├── auth/                                # 🔐 인증/인가 모듈
│   ├── controller/                      # 인증 REST API
│   ├── dto/                            # 인증 요청/응답 DTO
│   ├── service/                        # 인증 비즈니스 로직
│   ├── security/                       # Spring Security 설정
│   └── provider/                       # JWT 토큰 제공자
├── domain/                             # 🎯 도메인 계층
│   ├── user/                          # 사용자 도메인
│   │   ├── entity/                    # 사용자 엔티티
│   │   ├── repository/                # 사용자 리포지토리
│   │   └── service/                   # 사용자 도메인 서비스
│   └── moment/                        # 추억 도메인
│       ├── entity/                    # 추억 엔티티
│       ├── repository/                # 추억 리포지토리
│       └── service/                   # 추억 도메인 서비스
└── global/                            # 🌐 글로벌 설정
    ├── config/                        # 설정 클래스
    ├── exception/                     # 전역 예외 처리
    └── common/                        # 공통 기본 클래스
```

### 레이어별 책임

- **Controller**: REST API 엔드포인트, 요청/응답 처리
- **Service**: 비즈니스 로직, 트랜잭션 관리
- **Repository**: 데이터 접근 계층, JPA 인터페이스
- **Entity**: 도메인 모델, 데이터베이스 매핑
- **DTO**: 데이터 전송 객체, API 요청/응답 구조

## 📝 코드 컨벤션

### Kotlin 기본 규칙

```kotlin
// ✅ Good - 명확한 클래스 구조
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false)
    val email: String,
    
    @Column(nullable = false) 
    val name: String
) : BaseEntity() {
    
    companion object {
        fun createUser(email: String, name: String): User {
            return User(email = email, name = name)
        }
    }
}

// ✅ Good - 명확한 함수명과 타입
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    
    fun findUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    @Transactional
    fun createUser(request: CreateUserRequest): User {
        // 비즈니스 로직
    }
}
```

### Spring Boot 어노테이션

```kotlin
// Controller
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController

// Service  
@Service
@Transactional(readOnly = true)
class UserService

// Repository
@Repository
interface UserRepository : JpaRepository<User, String>
```

### 네이밍 규칙

- **클래스**: PascalCase (`UserService`, `MomentController`)
- **함수/변수**: camelCase (`findUser`, `createMoment`)
- **상수**: UPPER_SNAKE_CASE (`MAX_FILE_SIZE`)
- **패키지**: lowercase (`com.challkathon.momento`)

## 📋 커밋 컨벤션

### 기본 형식

```
<type>[scope]: <description>

[optional body]

[optional footer]
```

### 커밋 타입

| Type       | 설명        | 예시                                      |
|------------|-----------|-----------------------------------------|
| `feat`     | 새로운 기능 추가 | `feat(auth): JWT 토큰 인증 구현`              |
| `fix`      | 버그 수정     | `fix(user): 이메일 중복 검증 오류 수정`            |
| `docs`     | 문서 변경     | `docs: API 문서 업데이트`                     |
| `style`    | 코드 포맷팅    | `style: Kotlin 코드 스타일 적용`               |
| `refactor` | 코드 리팩토링   | `refactor(service): UserService 메서드 분리` |
| `test`     | 테스트 추가/수정 | `test(auth): JWT 토큰 검증 테스트 추가`          |
| `chore`    | 빌드/설정 변경  | `chore: Gradle 의존성 업데이트`                |

### 커밋 메시지 예시

```bash
# ✅ Good
feat(auth): 카카오 소셜 로그인 구현

- OAuth2 카카오 로그인 연동
- 사용자 정보 자동 회원가입
- JWT 토큰 발급 및 쿠키 설정

Resolves: #123

# ✅ Good  
fix(moment): 이미지 업로드 용량 제한 수정

업로드 가능한 이미지 크기를 10MB로 증가

# ❌ Bad
update user service
Fix bug
add new feature
```

### 브랜치 네이밍

```bash
# 기능 개발
feature/auth-jwt-implementation
feature/moment-image-upload
feature/user-profile-management

# 버그 수정  
bugfix/123-email-validation
bugfix/image-upload-error

# 핫픽스
hotfix/critical-security-patch
```

## 🔄 개발 워크플로우

### 1. 기능 개발 프로세스

```bash
# 1. 기능 브랜치 생성
git checkout -b feature/moment-create

# 2. 개발 진행
# 코드 작성 → 테스트 → 커밋

# 3. 코드 품질 검사
./gradlew ktlintCheck
./gradlew test

# 4. Push 및 PR 생성
git push origin feature/moment-create
```

### 2. Pull Request 체크리스트

- [ ] 빌드 및 테스트 성공
- [ ] 코드 스타일 검사 통과
- [ ] API 문서 업데이트 (필요시)
- [ ] 커밋 메시지 컨벤션 준수

### 3. 코드 리뷰 기준

- **기능성**: 요구사항 충족 및 예외 처리
- **가독성**: 명확한 네이밍과 구조
- **성능**: 불필요한 쿼리나 로직 최적화
- **보안**: 인증/인가 및 입력값 검증

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

<p align="center">
  <strong>CAFFEINEADDICT Team</strong> - 2025 CHALLKATHON
</p>

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
