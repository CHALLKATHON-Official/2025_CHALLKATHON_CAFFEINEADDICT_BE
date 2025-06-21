# 🚀 Momento Docker 배포 가이드

## 📋 사전 요구사항

### 1. EC2 인스턴스 설정
- **인스턴스 타입**: t3.medium 이상 권장
- **OS**: Ubuntu 22.04 LTS
- **보안 그룹**: 80, 443, 22 포트 오픈
- **IAM 역할**: EC2에서 S3 및 SSM 접근 권한 필요

### 2. 도메인 설정
- 도메인을 EC2 인스턴스의 Elastic IP에 연결
- DNS A 레코드 설정 완료

### 3. GitHub 저장소 설정
- GitHub Packages (ghcr.io) 사용 권한
- GitHub Actions secrets 설정

## 🔧 초기 서버 설정

### 1. EC2 인스턴스에 Docker 설치

```bash
#!/bin/bash
# Docker 설치 스크립트

# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# Docker 설치
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Docker 사용자 권한 설정
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker

# AWS CLI 설치
sudo apt install -y awscli

# 작업 디렉토리 생성
sudo mkdir -p /opt/momento
sudo chown -R $USER:$USER /opt/momento
```

### 2. GitHub Actions Secrets 설정

다음 secrets를 GitHub 저장소에 설정해야 합니다:

```
# AWS 관련
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=ap-northeast-2
DEPLOYMENT_BUCKET=your-s3-bucket-name
EC2_INSTANCE_ID=i-xxxxxxxxx

# JWT 및 소셜 로그인 (Spring Boot 환경변수로 주입)
JWT_SECRET=your_very_long_jwt_secret_key
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# SSL 및 도메인
DOMAIN_NAME=your-domain.com
SSL_EMAIL=your-email@domain.com
```

**중요**: 데이터베이스 설정은 `application-dev.yml`에서 직접 관리되며, Docker Compose에서 하드코딩된 값을 사용합니다.

## 🐳 Docker 기반 아키텍처

### 서비스 구성
- **nginx**: 리버스 프록시 + SSL 터미네이션
- **momento-server**: Spring Boot 애플리케이션
- **mysql**: 데이터베이스
- **certbot**: Let's Encrypt SSL 인증서 관리

### 네트워크 구조
```
Internet → Nginx (443/80) → Momento Server (8080) → MySQL (3306)
```

### GitHub Actions 기반 CI/CD

#### 워크플로우 구조

```yaml
name: CI/CD Pipeline

jobs:
  test:           # 🧪 테스트 & 코드 품질 검사
    - Unit Tests
    - Integration Tests  
    - Code Quality (ktlint, detekt)
    - Security Scan
    
  build:          # 🏗️ 애플리케이션 빌드
    - Gradle Build
    - JAR 생성
    - Artifact 업로드
    
  deploy-dev:     # 🚀 개발 서버 배포 (develop 브랜치)
    - JAR S3 업로드
    - EC2 배포 (AWS SSM)
    - Health Check
    
  deploy-prod:    # 🌟 프로덕션 배포 (main 브랜치)
    - JAR S3 업로드  
    - EC2 배포 (AWS SSM)
    - Health Check
    - 백업 & 롤백 지원
```

#### 환경별 트리거

- **develop 브랜치**: 개발 서버 자동 배포
- **main 브랜치**: 프로덕션 서버 자동 배포 (승인 필요)
- **Pull Request**: 테스트 및 보안 스캔만 실행

---

## 🔧 AWS 인프라 설정

### EC2 인스턴스 준비

#### 1. 필수 구성요소 설치

```bash
# Java 17 설치
sudo yum update -y
sudo amazon-linux-extras install java-openjdk17

# AWS CLI 설치 (최신 버전)
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# SSM Agent 확인 (Amazon Linux 2는 기본 설치됨)
sudo systemctl status amazon-ssm-agent
```

#### 2. 애플리케이션 사용자 및 디렉토리 생성

```bash
# momento 사용자 생성
sudo useradd -r -s /bin/false momento

# 애플리케이션 디렉토리 생성
sudo mkdir -p /opt/momento
sudo chown momento:momento /opt/momento

# 로그 디렉토리 생성
sudo mkdir -p /var/log/momento
sudo chown momento:momento /var/log/momento
```

#### 3. systemd 서비스 파일 생성

```bash
sudo tee /etc/systemd/system/momento.service > /dev/null <<EOF
[Unit]
Description=Momento Spring Boot Application
After=network.target
Wants=network.target

[Service]
Type=simple
User=momento
Group=momento
ExecStart=/usr/bin/java -jar /opt/momento/momento.jar
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=dev
EnvironmentFile=-/etc/systemd/system/momento.service.d/environment.conf

# 보안 설정
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/momento /var/log/momento

# 리소스 제한
LimitNOFILE=65536
MemoryMax=2G

[Install]
WantedBy=multi-user.target
EOF

# 서비스 활성화
sudo systemctl daemon-reload
sudo systemctl enable momento
```

#### 4. IAM Role 설정

EC2 인스턴스에 다음 권한을 가진 IAM Role을 연결하세요:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ssm:SendCommand",
                "ssm:GetCommandInvocation",
                "ssm:UpdateInstanceInformation",
                "ssm:SendCommand",
                "s3:GetObject"
            ],
            "Resource": "*"
        }
    ]
}
```

---

## 🔐 GitHub Secrets 설정

### Repository Secrets

GitHub Repository → Settings → Secrets and variables → Actions

#### AWS 관련 Secrets

| Secret Name | 설명 | 필수 여부 |
|-------------|------|----------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | ✅ |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | ✅ |
| `AWS_REGION` | AWS 리전 (예: ap-northeast-2) | ✅ |
| `DEPLOYMENT_BUCKET` | S3 배포 버킷명 | ✅ |

#### 인스턴스 관련 Secrets

| Secret Name | 설명 | 필수 여부 |
|-------------|------|----------|
| `DEV_EC2_INSTANCE_ID` | 개발 서버 EC2 인스턴스 ID | ✅ |
| `PROD_EC2_INSTANCE_ID` | 프로덕션 서버 EC2 인스턴스 ID | ✅ |
| `DEV_APPLICATION_URL` | 개발 서버 URL | ✅ |
| `PROD_APPLICATION_URL` | 프로덕션 서버 URL | ✅ |

### Environment Secrets

#### Development Environment

GitHub Repository → Settings → Environments → development

| Secret Name | 설명 |
|-------------|------|
| `DEV_DATABASE_URL` | 개발 DB 연결 URL |
| `DEV_DB_USERNAME` | 개발 DB 사용자명 |
| `DEV_DB_PASSWORD` | 개발 DB 비밀번호 |
| `DEV_JWT_SECRET` | 개발환경 JWT 시크릿 |
| `DEV_DOMAIN` | 개발 서버 도메인 |
| `KAKAO_CLIENT_ID` | 카카오 클라이언트 ID |
| `KAKAO_CLIENT_SECRET` | 카카오 클라이언트 시크릿 |

#### Production Environment

GitHub Repository → Settings → Environments → production

| Secret Name | 설명 |
|-------------|------|
| `PROD_DATABASE_URL` | 프로덕션 DB 연결 URL |
| `PROD_DB_USERNAME` | 프로덕션 DB 사용자명 |
| `PROD_DB_PASSWORD` | 프로덕션 DB 비밀번호 |
| `PROD_JWT_SECRET` | 프로덕션 JWT 시크릿 |
| `PROD_DOMAIN` | 프로덕션 서버 도메인 |
| `PROD_KAKAO_CLIENT_ID` | 프로덕션 카카오 클라이언트 ID |
| `PROD_KAKAO_CLIENT_SECRET` | 프로덕션 카카오 클라이언트 시크릿 |

---

## 🚀 배포 프로세스

### 개발 서버 배포 (Development)

```bash
# 1. develop 브랜치로 변경
git checkout develop

# 2. 변경사항 커밋 및 푸시
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin develop

# 3. GitHub Actions 자동 실행
# - 테스트 실행
# - 빌드 수행
# - 개발 서버 배포
# - Health Check 수행
```

### 프로덕션 배포 (Production)

```bash
# 1. main 브랜치로 변경
git checkout main

# 2. develop 브랜치 병합
git merge develop

# 3. 태그 생성 (선택사항)
git tag -a v1.0.0 -m "Release version 1.0.0"

# 4. 푸시
git push origin main
git push origin --tags

# 5. GitHub Actions 자동 실행
# - 테스트 실행
# - 빌드 수행  
# - 프로덕션 서버 배포 (승인 필요)
# - Health Check 수행
```

### 배포 상태 확인

#### GitHub Actions 로그 확인
1. GitHub Repository → Actions 탭
2. 해당 워크플로우 클릭
3. 각 단계별 로그 확인

#### 서버 상태 확인
```bash
# 서비스 상태 확인
sudo systemctl status momento

# 애플리케이션 로그 확인
sudo journalctl -u momento -f

# Health Check
curl http://your-domain/actuator/health
```

---

## 🔧 배포 중 문제 해결

### 일반적인 배포 문제

#### 1. SSM 명령 실행 실패

**증상:**
```
AccessDenied: User is not authorized to perform: ssm:SendCommand
```

**해결방법:**
- IAM 사용자/역할에 SSM 권한 확인
- EC2 인스턴스에 적절한 IAM Role 연결
- SSM Agent 실행 상태 확인

#### 2. 환경변수 설정 실패

**증상:**
```
Could not resolve placeholder 'JWT_SECRET'
```

**해결방법:**
- GitHub Secrets 설정 확인
- systemd 환경변수 파일 권한 확인
- 서비스 재시작 후 로그 확인

#### 3. Health Check 실패

**증상:**
```
❌ Health check failed after 2 minutes
```

**해결방법:**
```bash
# 서비스 로그 확인
sudo journalctl -u momento --no-pager -l

# 포트 확인
sudo netstat -tlnp | grep 8080

# 방화벽 설정 확인
sudo firewall-cmd --list-ports
```

### 롤백 프로세스

#### 자동 롤백 (프로덕션만)

GitHub Actions에서 Health Check 실패 시:
1. 이전 버전 JAR 파일로 자동 복원
2. 서비스 재시작
3. Health Check 재수행

#### 수동 롤백

```bash
# 1. 백업된 JAR 파일 확인
ls -la /opt/momento/momento-backup-*.jar

# 2. 이전 버전으로 복원
sudo cp /opt/momento/momento-backup-20240101_120000.jar /opt/momento/momento.jar

# 3. 서비스 재시작
sudo systemctl restart momento

# 4. 상태 확인
sudo systemctl status momento
```

---

## 📊 모니터링 및 운영

### 로그 관리

#### 애플리케이션 로그
```bash
# 실시간 로그 확인
sudo journalctl -u momento -f

# 특정 시간대 로그
sudo journalctl -u momento --since "2024-01-01 00:00:00" --until "2024-01-01 23:59:59"

# 에러 로그만 확인
sudo journalctl -u momento -p err
```

#### 로그 로테이션 설정
```bash
# /etc/logrotate.d/momento 파일 생성
sudo tee /etc/logrotate.d/momento > /dev/null <<EOF
/var/log/momento/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 momento momento
    postrotate
        systemctl reload momento || true
    endscript
}
EOF
```

### 성능 모니터링

#### 시스템 리소스 확인
```bash
# CPU 및 메모리 사용량
top -p $(pgrep -f momento)

# 네트워크 연결 상태
ss -tulnp | grep :8080

# 디스크 사용량
df -h
```

#### 애플리케이션 메트릭
- **Actuator Endpoints**: `/actuator/metrics`
- **Health Check**: `/actuator/health`
- **Info**: `/actuator/info`

### 백업 전략

#### 자동 백업 (배포 시)
- 배포 스크립트에서 자동으로 이전 버전 백업
- 파일명 형식: `momento-backup-YYYYMMDD_HHMMSS.jar`
- 최근 10개 버전 유지

#### 데이터베이스 백업
```bash
# 매일 새벽 2시 데이터베이스 백업
0 2 * * * mysqldump -u backup_user -p'password' momento_db > /backup/momento_$(date +\%Y\%m\%d).sql
```

---

## 🔒 보안 고려사항

### 네트워크 보안
- **방화벽 설정**: 필요한 포트만 오픈 (80, 443, 22)
- **SSL/TLS**: HTTPS 강제 사용
- **Security Groups**: AWS Security Group으로 접근 제한

### 애플리케이션 보안
- **환경변수**: 모든 민감정보를 환경변수로 관리
- **JWT Secret**: 256비트 이상 강력한 키 사용
- **정기 업데이트**: 의존성 및 보안 패치 정기 적용

### 접근 제어
- **IAM**: 최소 권한 원칙 적용
- **SSH Key**: 키 기반 인증만 허용
- **Audit Log**: 모든 배포 및 시스템 변경 로그 기록

---

## 📞 지원 및 문의

### 배포 관련 문의
- **GitHub Issues**: 배포 중 발생한 문제 리포트
- **팀 Slack**: `#momento-ops` 채널
- **긴급 상황**: 온콜 담당자 연락

### 유용한 명령어 참조

```bash
# 서비스 관리
sudo systemctl start momento
sudo systemctl stop momento  
sudo systemctl restart momento
sudo systemctl status momento

# 로그 확인
sudo journalctl -u momento -f
sudo tail -f /var/log/momento/application.log

# 배포 확인
curl -f http://localhost:8080/actuator/health
curl -f http://localhost:8080/actuator/info
```

---

<div align="center">

**🚀 안정적이고 확장 가능한 배포 시스템**

**☁️ GitHub Actions + AWS 클라우드 네이티브 배포**

Made with ❤️ by MOMENTO Team

</div>