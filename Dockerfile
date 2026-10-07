# --- 1. Build Stage ---
# Gradle과 JDK 21이 설치된 이미지를 빌드 환경으로 사용
FROM gradle:jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Backend 프로젝트 파일들을 복사
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
# 종속성 다운로드 (소스코드 변경 시 이 부분은 재실행되지 않음)
RUN ./gradlew dependencies

# 소스코드 전체 복사
COPY src ./src

# 테스트를 제외하고 애플리케이션 빌드
RUN ./gradlew build -x test

# --- 2. Run Stage ---
# Actual execution with JRE
FROM eclipse-temurin:21-jre

WORKDIR /app

# Scouter Agent 다운로드 및 설정
ADD https://github.com/scouter-project/scouter/releases/download/v2.20.0/scouter-all-2.20.0.tar.gz /tmp/scouter.tar.gz
RUN tar -xvf /tmp/scouter.tar.gz -C /app && \
    rm /tmp/scouter.tar.gz && \
    mkdir -p /app/scouter/agent.java/conf && \
    echo "net_collector_ip=host.docker.internal" > /app/scouter/agent.java/conf/scouter.conf

# Build Stage에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar /app/application.jar

# GitHub Actions에서 빌드된 프론트엔드 파일을 Spring Boot가 인식할 수 있는 위치로 복사
# (백엔드 단독 테스트 시에는 주석 처리하거나 해당 폴더가 있어야 함)
# RUN mkdir -p /app/static
# COPY frontend/dist/* /app/static/

# 애플리케이션 포트 노출
EXPOSE 8080

# 컨테이너 실행 시 prod 프로파일로 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar --spring.profiles.active=prod"]