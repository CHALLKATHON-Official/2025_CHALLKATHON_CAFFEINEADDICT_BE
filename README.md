# 🎊 Momento - 2025 CHALLKATHON

> 추억을 기록하고 공유하는 소셜 플랫폼

## 📋 프로젝트 개요

**Momento**는 사용자들이 소중한 순간들을 기록하고, 친구들과 추억을 공유할 수 있는 소셜 플랫폼입니다.

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

| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat(auth): JWT 토큰 인증 구현` |
| `fix` | 버그 수정 | `fix(user): 이메일 중복 검증 오류 수정` |
| `docs` | 문서 변경 | `docs: API 문서 업데이트` |
| `style` | 코드 포맷팅 | `style: Kotlin 코드 스타일 적용` |
| `refactor` | 코드 리팩토링 | `refactor(service): UserService 메서드 분리` |
| `test` | 테스트 추가/수정 | `test(auth): JWT 토큰 검증 테스트 추가` |
| `chore` | 빌드/설정 변경 | `chore: Gradle 의존성 업데이트` |

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

