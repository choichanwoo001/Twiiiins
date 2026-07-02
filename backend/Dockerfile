# 멀티스테이지 빌드
FROM gradle:8.14.3-jdk17 AS builder

# Gradle 홈 디렉토리를 최소한으로 설정하여 디스크 공간 절약
ENV GRADLE_USER_HOME=/tmp/.gradle
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.configureondemand=false -Dorg.gradle.parallel=false"

WORKDIR /app

# Gradle 의존성 캐싱을 위해 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN chmod +x gradlew

# 의존성 다운로드 (캐시 레이어 활용)
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# 소스 코드만 복사 (build, bin, uploads 등 제외)
COPY src ./src
COPY build.gradle settings.gradle ./

# 빌드 및 즉시 캐시 정리 (디스크 공간 절약)
RUN ./gradlew clean build -x test --no-daemon \
    && rm -rf /app/.gradle \
    && rm -rf /tmp/.gradle/caches \
    && rm -rf /tmp/.gradle/wrapper \
    && rm -rf /app/build/classes \
    && rm -rf /app/build/generated \
    && rm -rf /app/build/tmp \
    && rm -rf /app/build/resources \
    && rm -rf /app/build/reports \
    && find /app/build -type f ! -name "*.jar" -delete \
    && find /app/build -type d -empty -delete \
    && find /app/build/libs -type f ! -name "twiiiins-backend-*.jar" -delete \
    && rm -rf /tmp/.gradle \
    || (rm -rf /app/.gradle && rm -rf /tmp/.gradle && exit 1)

# 실행 스테이지
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install curl for runtime health checks
USER root
RUN apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get update --fix-missing \
  && apt-get install -y --no-install-recommends curl \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# 비-루트 유저 생성
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring

# 빌드된 JAR 파일 복사 (빌드 아티팩트만)
COPY --from=builder --chown=spring:spring /app/build/libs/twiiiins-backend-*.jar app.jar

# 환경 변수 기본값 설정
ENV PORT=8080 \
    JAVA_OPTS="-Xmx512m -Xms256m"

# 포트 노출
EXPOSE ${PORT}

# 헬스체크 추가 (curl 사용)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:${PORT}/actuator/health > /dev/null || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
