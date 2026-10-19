FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="zbm-platform" \
      org.opencontainers.image.description="零界物业管理系统后端运行环境"

RUN addgroup -S zbm \
    && adduser -S -G zbm -h /app zbm \
    && mkdir -p /app/logs /app/temp \
    && chown -R zbm:zbm /app

WORKDIR /app

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseZGC -XX:+HeapDumpOnOutOfMemoryError"

USER zbm
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -q -O /dev/null "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "test -r /app/ruoyi-admin.jar && exec java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar /app/ruoyi-admin.jar"]
