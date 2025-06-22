# 멀티스테이지 빌드를 위한 Dockerfile
FROM gradle:8.10.1-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위해 build.gradle.kts와 설정 파일들 먼저 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon

# 소스코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# 런타임 스테이지
FROM openjdk:17-jre-alpine

# 로그 디렉토리 생성
RUN mkdir -p /var/log/momento

# 애플리케이션 사용자 생성 (보안)
RUN addgroup -g 1001 momento && \
    adduser -D -s /bin/sh -u 1001 -G momento momento

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar /app/momento.jar

# 파일 권한 설정
RUN chown -R momento:momento /app /var/log/momento

# 사용자 변경
USER momento

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/momento.jar"]