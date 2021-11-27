FROM ubuntu:20.04

RUN apt-get update
RUN apt install -y openjdk-13-jre-headless && apt install -y openjdk-13-jdk-headless
RUN apt-get install -y musl-dev
RUN apt install maven -y

COPY src /home/app/src
COPY libs /home/app/libs
COPY pom.xml /home/app
COPY Logs /usr/local/lib/Logs

COPY config.txt /usr/local/lib/config.txt
RUN export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

RUN mvn install:install-file -Dfile="/home/app/libs/repo/jep.jar" -DgroupId=jep -DartifactId=jep -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
RUN mvn install:install-file -Dfile="/home/app/libs/repo/javassist.jar" -DgroupId=javassist -DartifactId=javassist -Dversion=2.7 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
RUN mvn install:install-file -Dfile="/home/app/libs/repo/config-1.2.1.jar" -DgroupId=config -DartifactId=config -Dversion=1.2.1 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
RUN mvn install:install-file -Dfile="/home/app/libs/repo/google-translate-095.jar" -DgroupId=google-translate -DartifactId=google-translate -Dversion=0.95 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true

RUN mvn -f /home/app/pom.xml clean package


RUN mv /home/app/target/Game-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/app.jar

WORKDIR /usr/local/lib/

ENTRYPOINT ["java","-Xmx1024M", "-jar","app.jar", "-o", "true"]
