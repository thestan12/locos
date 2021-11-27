FROM ubuntu:20.04

RUN apt-get update

RUN apt install -y openjdk-13-jre-headless && apt install -y openjdk-13-jdk-headless

RUN apt-get install -y musl-dev
RUN apt install maven -y

COPY src /home/app/src
COPY libs /home/app/libs
COPY pom.xml /home/app

COPY config.properties /usr/local/lib/config.properties

RUN mvn -f /home/app/pom.xml clean package
RUN mv /home/app/target/starloco-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/app.jar
WORKDIR /usr/local/lib/
ENTRYPOINT ["java","-Xmx1024M", "-jar","app.jar", "-o", "true"]
