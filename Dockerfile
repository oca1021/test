# 1단계: 빌드 환경 (Build stage)
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# gradlew에 실행 권한 부여 및 빌드 (테스트는 배포 속도를 위해 제외)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test --no-daemon

# 2단계: 실행 환경 (Run stage)
FROM openjdk:17-slim
EXPOSE 8080

# 빌드 단계에서 생성된 jar 파일만 복사
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]