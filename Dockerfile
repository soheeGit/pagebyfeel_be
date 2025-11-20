FROM eclipse-temurin:21-jdk-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build.gradle 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (레이어 캐싱을 위해 별도로 실행)
RUN ./gradlew dependencies --no-daemon --parallel

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon --parallel

# 실행 단계
FROM eclipse-temurin:21-jre-jammy

# 메타데이터 설정
LABEL maintainer="pagebyfeel-team"
LABEL version="1.0"
LABEL description="pagebyfeel Backend Application"

# 작업 디렉토리 설정
WORKDIR /app

# Healthcheck에 필요한 curl 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# JAR 파일 복사 (특정 패턴으로 수정)
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]