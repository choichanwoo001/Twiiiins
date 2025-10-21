# 멀티스테이지 빌드
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

# Gradle 빌드
RUN ./gradlew build -x test

# 실행 스테이지
FROM openjdk:17-jre-slim

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]
