FROM maven:3.8.6-openjdk-8

WORKDIR /app

COPY pom.xml .
COPY src/main ./src/main

RUN mkdir -p src/generated && \
    idlj -fall -td src/generated src/main/java/com/pdfcorba/PDFService.idl

RUN mvn package -DskipTests

# Exposer les deux ports
EXPOSE 1050
EXPOSE 8090

CMD orbd -ORBInitialPort 1050 & \
    sleep 3 && \
    java -cp target/corba-server-1.0-SNAPSHOT-jar-with-dependencies.jar \
         com.pdfcorba.CORBAServer \
         -ORBInitialHost localhost \
         -ORBInitialPort 1050
