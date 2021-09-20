FROM maven:3.8.2-openjdk-16
VOLUME /tmp
ARG JAR_FILE
WORKDIR /app

COPY pom.xml pom.xml
COPY quiz-server/pom.xml quiz-server/pom.xml
COPY quiz-server/src quiz-server/src

RUN mvn clean package -DskipTests

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker","-jar","quiz-server/target/quiz-server-0.0.1-SNAPSHOT.jar"]