# GitHub Secrets 설정 가이드

## 🔐 개요

momento 프로젝트는 GitHub Actions를 통한 자동 배포를 위해 GitHub Secrets를 사용합니다.
이 문서는 필요한 모든 Secrets 설정 방법을 안내합니다.

## 📋 설정해야 할 Secrets 목록

### ☁️ AWS 관련 Secrets

| Secret Name             | 설명            | 예시 값                                       |
|-------------------------|---------------|--------------------------------------------|
| `AWS_ACCESS_KEY_ID`     | AWS 액세스 키 ID  | `AKIAIOSFODNN7EXAMPLE`                     |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| `AWS_REGION`            | AWS 리전        | `ap-northeast-2` (서울)                      |
| `DEPLOYMENT_BUCKET`     | 배포용 S3 버킷명    | `momento-deployment-bucket`                |

### 🖥️ EC2 인스턴스 관련 Secrets

| Secret Name            | 설명                  | 예시 값                  |
|------------------------|---------------------|-----------------------|
| `DEV_EC2_INSTANCE_ID`  | 개발 서버 EC2 인스턴스 ID   | `i-1234567890abcdef0` |
| `PROD_EC2_INSTANCE_ID` | 프로덕션 서버 EC2 인스턴스 ID | `i-0987654321fedcba0` |

### 🌐 애플리케이션 URL Secrets

| Secret Name            | 설명                 | 예시 값                          |
|------------------------|--------------------|-------------------------------|
| `DEV_APPLICATION_URL`  | 개발 서버 애플리케이션 URL   | `https://dev-api.momento.com` |
| `PROD_APPLICATION_URL` | 프로덕션 서버 애플리케이션 URL | `https://api.momento.com`     |

## 🌍 Environment-specific Secrets

GitHub Repository → Settings → Environments에서 환경별 Secrets를 관리할 수 있습니다.

### Development Environment

Environment name: `development`

| Secret Name           | 설명            | 
|-----------------------|---------------|
| `DEV_DATABASE_URL`    | 개발 DB 연결 URL  |
| `DEV_DB_USERNAME`     | 개발 DB 사용자명    |
| `DEV_DB_PASSWORD`     | 개발 DB 비밀번호    |
| `DEV_JWT_SECRET`      | 개발 환경 JWT 시크릿 |
| `DEV_DOMAIN`          | 개발 서버 도메인     |
| `KAKAO_CLIENT_ID`     | 카카오 클라이언트 ID  |
| `KAKAO_CLIENT_SECRET` | 카카오 클라이언트 시크릿 |

### Production Environment

Environment name: `production`

| Secret Name                | 설명                 |
|----------------------------|--------------------|
| `PROD_DATABASE_URL`        | 프로덕션 DB 연결 URL     |
| `PROD_DB_USERNAME`         | 프로덕션 DB 사용자명       |
| `PROD_DB_PASSWORD`         | 프로덕션 DB 비밀번호       |
| `PROD_JWT_SECRET`          | 프로덕션 JWT 시크릿       |
| `PROD_DOMAIN`              | 프로덕션 서버 도메인        |
| `PROD_KAKAO_CLIENT_ID`     | 프로덕션 카카오 클라이언트 ID  |
| `PROD_KAKAO_CLIENT_SECRET` | 프로덕션 카카오 클라이언트 시크릿 |

## 🛠️ Secrets 설정 단계별 가이드

### 1. Repository Secrets 설정

1. GitHub 저장소 페이지로 이동
2. `Settings` 탭 클릭
3. 좌측 사이드바에서 `Secrets and variables` → `Actions` 클릭
4. `New repository secret` 버튼 클릭
5. Secret name과 value 입력 후 `Add secret` 클릭

### 2. Environment Secrets 설정

1. GitHub 저장소의 `Settings` → `Environments` 이동
2. `New environment` 클릭 후 `development` 입력
3. `Configure environment` 클릭
4. `Environment secrets` 섹션에서 필요한 secrets 추가
5. `production` 환경도 동일하게 생성

### 3. Environment Protection Rules (선택사항)

프로덕션 환경의 보안을 강화하려면:

1. `production` 환경 설정에서 `Environment protection rules` 활성화
2. `Required reviewers` 설정 - 배포 전 승인 필요
3. `Wait timer` 설정 - 배포 전 대기 시간
4. `Deployment branches` 설정 - 특정 브랜치만 배포 허용

## 🔧 AWS 설정 준비사항

### IAM 사용자 생성

배포용 IAM 사용자를 생성하고 다음 권한을 부여하세요:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ssm:SendCommand",
        "ssm:GetCommandInvocation",
        "ssm:DescribeInstanceInformation",
        "s3:PutObject",
        "s3:GetObject",
        "ec2:DescribeInstances"
      ],
      "Resource": "*"
    }
  ]
}
```

### EC2 인스턴스 준비

1. **SSM Agent 설치**: EC2 인스턴스에 AWS Systems Manager Agent 설치
2. **IAM Role 연결**: EC2 인스턴스에 SSM 접근 권한이 있는 IAM Role 연결
3. **애플리케이션 디렉토리 생성**: `/opt/momento` 디렉토리 생성
4. **systemd 서비스 파일** 작성:

```ini
# /etc/systemd/system/momento.service
[Unit]
Description=Momento Spring Boot Application
After=network.target

[Service]
Type=simple
User=momento
ExecStart=/usr/bin/java -jar /opt/momento/momento.jar
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=dev

[Install]
WantedBy=multi-user.target
```

### S3 배포 버킷 생성

```bash
# AWS CLI로 배포용 S3 버킷 생성
aws s3 mb s3://momento-deployment-bucket --region ap-northeast-2
```

## ⚠️ 보안 주의사항

### 1. Secrets 값 생성 가이드

```bash
# JWT Secret 생성 (256비트)
openssl rand -base64 32

# DB 비밀번호 생성
openssl rand -base64 16
```

### 2. Secrets 관리 모범 사례

- ✅ **주기적 로테이션**: JWT Secret 및 DB 비밀번호를 정기적으로 변경
- ✅ **최소 권한 원칙**: IAM 사용자에게 필요한 최소한의 권한만 부여
- ✅ **환경 분리**: 개발/프로덕션 환경의 Secrets 완전 분리
- ✅ **접근 로그 모니터링**: AWS CloudTrail을 통한 접근 로그 모니터링

### 3. 절대 하지 말아야 할 것들

- ❌ **코드에 하드코딩**: Secret 값을 코드에 직접 작성
- ❌ **로그 출력**: Secret 값을 로그에 출력
- ❌ **개인 계정 사용**: 개인 AWS 계정 정보를 팀 프로젝트에 사용
- ❌ **브라우저 저장**: Secret 값을 브라우저에 저장

## 🚀 배포 플로우

### Development 배포

1. `develop` 브랜치에 push
2. GitHub Actions가 자동으로 테스트 및 빌드
3. Development environment secrets 사용하여 개발 서버 배포

### Production 배포

1. `main` 브랜치에 push (또는 PR merge)
2. GitHub Actions가 자동으로 테스트 및 빌드
3. Production environment의 보호 규칙 확인
4. 승인 후 Production environment secrets 사용하여 프로덕션 서버 배포

## 📞 문제 해결

### Secrets 관련 오류 해결

1. **Secret not found**: Secret 이름의 대소문자 및 철자 확인
2. **Permission denied**: IAM 권한 및 Role 설정 확인
3. **Environment not accessible**: Environment protection rules 및 브랜치 설정 확인

### 디버깅 팁

GitHub Actions 로그에서 다음과 같이 Secrets 사용 여부를 확인할 수 있습니다:

```yaml
- name: Debug Secrets
  run: |
    echo "JWT_SECRET length: ${#JWT_SECRET}"
    echo "AWS_REGION: ${{ secrets.AWS_REGION }}"
  env:
    JWT_SECRET: ${{ secrets.DEV_JWT_SECRET }}
```

> ⚠️ **주의**: 실제 Secret 값은 절대 출력하지 마세요!