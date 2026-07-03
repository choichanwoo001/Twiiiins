FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

USER root
RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

USER spring:spring

COPY --chown=spring:spring build/libs/twiiiins-backend-*.jar app.jar

ENV PORT=8080 \
    JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:${PORT}/actuator/health > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
