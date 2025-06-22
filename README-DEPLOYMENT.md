# Momento 배포 가이드

## 필요한 GitHub Secrets 설정

다음 환경변수들을 GitHub Repository Settings > Secrets and variables > Actions에서 설정해주세요:

### 필수 Secrets

#### 서버 배포 관련
- `EC2_HOST`: EC2 인스턴스의 공개 IP 주소
- `EC2_USER`: EC2 SSH 사용자명 (예: ubuntu, ec2-user)
- `EC2_SSH_KEY`: EC2 인스턴스 접근용 SSH private key

#### 애플리케이션 보안
- `JWT_SECRET`: JWT 토큰 서명용 시크릿 키 (최소 32자 랜덤 문자열)
- `KAKAO_CLIENT_ID`: Kakao OAuth 클라이언트 ID
- `KAKAO_CLIENT_SECRET`: Kakao OAuth 클라이언트 시크릿

#### 데이터베이스 보안
- `MYSQL_ROOT_PASSWORD`: MySQL 루트 사용자 비밀번호 (강력한 비밀번호)
- `MYSQL_PASSWORD`: momento_user 사용자 비밀번호 (강력한 비밀번호)
- `MYSQL_DATABASE`: 데이터베이스명 (기본값: momento_db)
- `MYSQL_USER`: 데이터베이스 사용자명 (기본값: momento_user)

#### SSL 인증서
- `LETSENCRYPT_EMAIL`: SSL 인증서 발급용 이메일 주소

### 보안 권장사항
- 모든 비밀번호는 최소 16자 이상, 대소문자/숫자/특수문자 조합
- JWT_SECRET은 최소 32자 이상의 랜덤 문자열 사용
- 정기적인 비밀번호 변경 (3-6개월마다)

## EC2 서버 사전 준비

### 1. Docker 및 Docker Compose 설치
```bash
# Docker 설치
sudo apt update
sudo apt install -y docker.io docker-compose

# Docker 서비스 시작 및 자동 시작 설정
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
```

### 2. 보안 그룹 설정
EC2 보안 그룹에서 다음 포트를 열어주세요:
- **80 (HTTP)**: Let's Encrypt 인증서 발급용
- **443 (HTTPS)**: 실제 서비스 포트
- **22 (SSH)**: GitHub Actions 배포용

### 3. 도메인 DNS 설정
가비아에서 구매한 도메인의 DNS 설정:
- **A 레코드**: `dev.caffeineoverdose.shop` → EC2 퍼블릭 IP

## 배포 과정

### 자동 배포 (GitHub Actions)
1. `develop` 브랜치에 코드 푸시
2. GitHub Actions가 자동으로 실행됨:
   - 테스트 실행
   - Docker 이미지 빌드
   - EC2로 파일 전송
   - SSL 인증서 발급 (최초 1회)
   - 컨테이너 배포
   - 헬스체크 확인

### 수동 배포 (선택사항)
EC2 서버에서 직접 실행:
```bash
# 저장소 클론
git clone <repository-url>
cd momento

# 환경변수 설정 (.env 파일 생성)
cp .env.example .env
# .env 파일을 편집하여 실제 값 입력

# 또는 직접 환경변수 export
export JWT_SECRET="your-jwt-secret"
export KAKAO_CLIENT_ID="your-kakao-client-id"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"
export MYSQL_ROOT_PASSWORD="your-root-password"
export MYSQL_PASSWORD="your-user-password"

# SSL 인증서 초기 발급 (최초 1회만)
./init-letsencrypt.sh

# 서비스 시작
docker-compose up -d
```

## 서비스 접근

### 웹 서비스
- **메인 사이트**: https://dev.caffeineoverdose.shop
- **Swagger UI**: https://dev.caffeineoverdose.shop/swagger-ui.html
- **헬스체크**: https://dev.caffeineoverdose.shop/actuator/health

### 컨테이너 관리
```bash
# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs momento-server
docker-compose logs nginx
docker-compose logs mysql

# 서비스 재시작
docker-compose restart momento-server

# 서비스 중지
docker-compose down

# 데이터까지 완전 삭제
docker-compose down -v
```

## 모니터링 및 유지보수

### SSL 인증서 자동 갱신
- Certbot 컨테이너가 12시간마다 인증서 갱신 체크
- nginx는 6시간마다 설정 리로드

### 로그 위치
- **애플리케이션 로그**: `./app_logs/` (Docker 볼륨)
- **nginx 로그**: `./nginx_logs/` (Docker 볼륨)
- **MySQL 데이터**: `./mysql_data/` (Docker 볼륨)

### 백업
```bash
# 데이터베이스 백업
docker-compose exec mysql mysqldump -u momento_user -p momento_db > backup.sql

# 전체 볼륨 백업
docker run --rm -v momento_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz -C /data .
```

## 트러블슈팅

### SSL 인증서 문제
```bash
# 인증서 상태 확인
docker-compose exec nginx ls -la /etc/letsencrypt/live/dev.caffeineoverdose.shop/

# 인증서 재발급
docker-compose run --rm certbot certonly --webroot -w /var/www/certbot -d dev.caffeineoverdose.shop --force-renewal
docker-compose exec nginx nginx -s reload
```

### 애플리케이션 문제
```bash
# 컨테이너 상태 확인
docker-compose ps

# 상세 로그 확인
docker-compose logs -f momento-server

# 컨테이너 재시작
docker-compose restart momento-server
```

### 데이터베이스 문제
```bash
# MySQL 접속
docker-compose exec mysql mysql -u momento_user -p momento_db

# 데이터베이스 상태 확인
docker-compose exec mysql mysqladmin -u root -p status
```