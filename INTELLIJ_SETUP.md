# IntelliJ IDEA 환경변수 설정 가이드

## 🎯 개요

momento 프로젝트는 .env 파일 대신 **IntelliJ IDEA의 내장 환경변수 관리 기능**을 사용합니다.
이를 통해 개발자별로 독립적인 환경설정을 관리할 수 있습니다.

## ⚙️ IntelliJ Run Configuration 설정

### 1. Run Configuration 열기

1. **메뉴 바**: `Run` → `Edit Configurations...`
2. **또는**: 우측 상단 실행 버튼 옆 드롭다운 → `Edit Configurations...`

### 2. Spring Boot Configuration 생성

1. 좌측 `+` 버튼 클릭
2. `Spring Boot` 선택
3. 다음 정보 입력:
   - **Name**: `MomentoApplication-local`
   - **Main class**: `com.challkathon.momento.MomentoApplicationKt`
   - **Active profiles**: `local`

### 3. Environment Variables 설정

`Environment Variables` 섹션에서 다음 환경변수들을 설정하세요:

#### 필수 환경변수

```bash
# JWT 보안 설정
JWT_SECRET=localDevSecretKeyForJwtTokenGenerationThatIsLongEnoughForHmac256Algorithm

# 데이터베이스 설정 (선택사항 - 기본값 있음)
DB_USERNAME=momento
DB_PASSWORD=momento123
```

#### OAuth2 설정 (선택사항)

소셜 로그인을 테스트하려면 다음 값들을 설정하세요:

```bash
# Kakao OAuth2
KAKAO_CLIENT_ID=your_actual_kakao_client_id
KAKAO_CLIENT_SECRET=your_actual_kakao_client_secret

# Google OAuth2 (미래 확장용)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# GitHub OAuth2 (미래 확장용)
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
```

### 4. 설정 완료 및 실행

1. `Apply` → `OK` 클릭
2. 우측 상단에서 `MomentoApplication-local` 선택
3. ▶️ 실행 버튼 클릭

## 🔧 고급 설정

### 다중 환경 Configuration 설정

개발, 테스트, 프로덕션 환경을 위한 별도 Configuration을 만들 수 있습니다:

#### 1. 테스트 Configuration
- **Name**: `MomentoApplication-test`
- **Active profiles**: `test`
- **Environment Variables**: 
  ```bash
  JWT_SECRET=testSecretKeyForJwtTokenGenerationThatIsLongEnoughForHmac256Algorithm
  ```

#### 2. 개발 서버 시뮬레이션 Configuration
- **Name**: `MomentoApplication-dev`
- **Active profiles**: `dev`
- **Environment Variables**: 개발 서버와 동일한 값들 설정

### 환경변수 템플릿 공유

팀원들과 환경변수 템플릿을 공유하려면:

1. **Export**: `Run` → `Export Configuration...`
2. **Import**: 팀원이 받은 파일을 `Run` → `Import Configuration...`

> ⚠️ **주의**: 실제 secret 값들은 공유하지 마세요. 템플릿만 공유하고 각자 실제 값을 설정하세요.

## 📋 환경변수 체크리스트

### ✅ 필수 설정 (로컬 개발)
- [ ] `JWT_SECRET` - JWT 토큰 서명용 시크릿 키
- [ ] `DB_USERNAME` - MySQL 사용자명 (기본값: momento)
- [ ] `DB_PASSWORD` - MySQL 비밀번호 (기본값: momento123)

### ✅ 선택적 설정 (OAuth2 테스트용)
- [ ] `KAKAO_CLIENT_ID` - 카카오 OAuth2 클라이언트 ID
- [ ] `KAKAO_CLIENT_SECRET` - 카카오 OAuth2 클라이언트 시크릿
- [ ] `GOOGLE_CLIENT_ID` - 구글 OAuth2 클라이언트 ID (미래용)
- [ ] `GOOGLE_CLIENT_SECRET` - 구글 OAuth2 클라이언트 시크릿 (미래용)

## 🔍 트러블슈팅

### 1. 환경변수가 인식되지 않는 경우

**증상**: `Could not resolve placeholder 'JWT_SECRET'` 오류

**해결방법**:
1. Run Configuration에서 환경변수가 올바르게 설정되었는지 확인
2. Active profiles가 `local`로 설정되었는지 확인
3. IntelliJ 재시작 후 다시 시도

### 2. 데이터베이스 연결 실패

**증상**: `Connection refused` 또는 `Access denied` 오류

**해결방법**:
1. MySQL 서버가 실행 중인지 확인
2. 데이터베이스 `momento_db`가 생성되었는지 확인
3. `DB_USERNAME`, `DB_PASSWORD` 환경변수 확인

### 3. OAuth2 리다이렉트 오류

**증상**: `redirect_uri_mismatch` 오류

**해결방법**:
1. OAuth2 제공자(카카오 등) 설정에서 리다이렉트 URI 확인:
   - 로컬: `http://localhost:8080/login/oauth2/code/kakao`
2. 클라이언트 ID와 Secret이 올바른지 확인

## 🚀 빠른 시작 가이드

1. **IntelliJ에서 프로젝트 열기**
2. **Run Configuration 생성**:
   - Name: `MomentoApplication-local`
   - Active profiles: `local`
   - Environment Variables에 `JWT_SECRET` 추가
3. **MySQL 실행 및 DB 생성**:
   ```sql
   CREATE DATABASE momento_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
4. **애플리케이션 실행** ▶️
5. **브라우저에서 확인**: http://localhost:8080/swagger-ui.html

## 💡 팁

### 환경변수 자동 완성
IntelliJ에서는 환경변수 입력 시 자동 완성을 제공합니다. `${}` 내부에서 Ctrl+Space를 누르면 사용 가능한 환경변수 목록을 볼 수 있습니다.

### 설정 백업
중요한 Run Configuration은 프로젝트 설정에 저장할 수 있습니다:
1. Run Configuration에서 `Store as project file` 체크
2. 이렇게 하면 `.idea/runConfigurations/` 폴더에 저장되어 팀원과 공유 가능

### 보안 주의사항
- 실제 production 환경의 secret 값들은 절대 IntelliJ 설정에 저장하지 마세요
- 로컬 개발용 더미 값만 사용하세요
- Git에 민감한 정보가 커밋되지 않도록 주의하세요