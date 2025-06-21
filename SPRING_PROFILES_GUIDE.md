# 🌱 Spring Boot Profile 기반 환경 관리 가이드

Momento 프로젝트는 `.env` 파일 대신 Spring Boot의 내장 Profile 기능을 사용하여 환경을 관리합니다.

## 📋 환경 구성 개요

### 환경별 설정 파일
- **`application-local.yml`**: IntelliJ 로컬 개발 환경
- **`application-dev.yml`**: Docker/EC2 배포 환경

### 환경 활성화 방법
- **로컬**: IntelliJ에서 `SPRING_PROFILES_ACTIVE=local` 설정
- **배포**: Docker 컨테이너에서 `SPRING_PROFILES_ACTIVE=dev` 설정

## 🖥️ 로컬 개발 환경 (IntelliJ)

### 1. Run Configuration 설정

1. **Run/Debug Configurations** 열기
2. **Spring Boot** 설정 생성
3. **Environment Variables** 섹션에서 다음 설정:

```
SPRING_PROFILES_ACTIVE=local
JWT_SECRET=your_local_jwt_secret_key_256_bits
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

### 2. application-local.yml 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/momento_local
    username: root
    password: your_local_password
  
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            redirect-uri: http://localhost:8080/login/oauth2/code/kakao

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 7200000    # 2시간 (개발용)

app:
  cors:
    allowed-origins: http://localhost:3000
  cookie:
    refresh-token:
      domain: localhost
      secure: false  # 로컬에서는 HTTP 허용
```

## 🐳 Docker 배포 환경

### 1. Docker Compose 설정

```yaml
services:
  momento-server:
    environment:
      SPRING_PROFILES_ACTIVE: dev
      # GitHub Actions에서 주입되는 환경변수들:
      # JWT_SECRET: ${JWT_SECRET}
      # KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID}
      # KAKAO_CLIENT_SECRET: ${KAKAO_CLIENT_SECRET}
```

### 2. application-dev.yml 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/momento_db
    username: momento_user
    password: momento_pass  # Docker 컨테이너 간 통신용

  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            redirect-uri: ${KAKAO_REDIRECT_URI:http://localhost:8080}/login/oauth2/code/kakao

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 3600000    # 1시간 (프로덕션용)

app:
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:http://localhost:3000}
  cookie:
    refresh-token:
      domain: ${COOKIE_DOMAIN:localhost}
      secure: true  # HTTPS 환경에서 true
```

## 🔧 GitHub Actions 환경변수 주입

### 1. CI/CD 파이프라인에서 환경변수 주입

```bash
# 배포 스크립트에서 docker-compose.yml 동적 수정
sed -i "/SPRING_PROFILES_ACTIVE: dev/a\\      JWT_SECRET: ${{ secrets.JWT_SECRET }}" docker-compose.yml
sed -i "/JWT_SECRET:/a\\      KAKAO_CLIENT_ID: ${{ secrets.KAKAO_CLIENT_ID }}" docker-compose.yml
sed -i "/KAKAO_CLIENT_ID:/a\\      KAKAO_CLIENT_SECRET: ${{ secrets.KAKAO_CLIENT_SECRET }}" docker-compose.yml
```

### 2. 필요한 GitHub Secrets

```
JWT_SECRET=your_production_jwt_secret_key
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
DOMAIN_NAME=your-domain.com
SSL_EMAIL=your-email@domain.com
```

## 🎯 환경별 특징 비교

| 설정 항목 | Local (local) | Deploy (dev) |
|-----------|---------------|--------------|
| **Profile** | `local` | `dev` |
| **데이터베이스** | 로컬 MySQL | Docker MySQL 컨테이너 |
| **JWT 만료시간** | 2시간 (개발용) | 1시간 (보안 강화) |
| **CORS** | localhost:3000 | 환경변수로 설정 |
| **Cookie Secure** | false (HTTP) | true (HTTPS) |
| **환경변수 관리** | IntelliJ | GitHub Actions → Docker |

## 🔍 환경별 확인 방법

### 로컬 환경 확인
```bash
curl http://localhost:8080/actuator/health
# 또는 IntelliJ 콘솔에서 "Active profiles: local" 확인
```

### 배포 환경 확인
```bash
docker-compose exec momento-server curl http://localhost:8080/actuator/health
# 또는 로그에서 "Active profiles: dev" 확인
```

## 🚨 주의사항

1. **환경변수 우선순위**: Spring Boot는 환경변수 → application.yml 순으로 설정을 적용합니다.
2. **보안**: 프로덕션 환경에서는 반드시 강력한 JWT Secret을 사용하세요.
3. **도메인 설정**: 배포 환경에서는 실제 도메인으로 CORS와 Redirect URI를 설정하세요.
4. **SSL**: 프로덕션에서는 `secure: true`로 설정하여 HTTPS만 허용하세요.

이 가이드를 따라 Spring Boot Profile 기반의 깔끔한 환경 관리를 구현할 수 있습니다.