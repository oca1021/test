# 1단계: 빌드 환경 (Build stage)
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# gradlew 실행 권한 부여 및 빌드
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test --no-daemon

# 2단계: 실행 환경 (Run stage)
# openjdk 대신 안정적인 eclipse-temurin 사용
FROM eclipse-temurin:17-jre
EXPOSE 8080

# 빌드 결과물 복사
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]