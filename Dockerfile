FROM maven AS builder

WORKDIR /home

COPY pom.xml pom.xml
COPY quiz-server/pom.xml quiz-server/pom.xml
COPY quiz-server/src quiz-server/src

RUN mvn clean package -DskipTests

#############################################

FROM openjdk:17-alpine

WORKDIR /home

COPY --from=builder /home/quiz-server/target/quiz-server-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker","-jar","app.jar"]