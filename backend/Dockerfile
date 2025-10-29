# 멀티스테이지 빌드
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle 의존성 캐싱을 위해 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY . .
RUN ./gradlew build -x test --no-daemon

# 실행 스테이지
FROM openjdk:17-jre-slim

WORKDIR /app

# 비-루트 유저 생성
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경 변수 기본값 설정
ENV PORT=8080 \
    JAVA_OPTS="-Xmx512m -Xms256m"

# 포트 노출
EXPOSE ${PORT}

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
