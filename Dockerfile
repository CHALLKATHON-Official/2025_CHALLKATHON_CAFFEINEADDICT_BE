# 멀티스테이지 빌드를 위한 Dockerfile
FROM gradle:8.7-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위해 build.gradle 파일들을 먼저 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

# 의존성 다운로드 (Docker 레이어 캐싱 활용)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# 프로덕션 이미지
FROM openjdk:17-jre-slim

# 비root 사용자 생성
RUN groupadd -r momento && useradd -r -g momento momento

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown -R momento:momento /app

# 비root 사용자로 전환
USER momento

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 설정
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseContainerSupport"

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]