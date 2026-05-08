# Java 8 obligatoire pour CORBA natif
# contient  Maven + Java 8 préinstallés ensemble, sans passer par apk
FROM maven:3.8.6-openjdk-8

WORKDIR /app

# Copier le pom.xml et les sources qui contiennent deja les fichiers générés par idlj
COPY pom.xml .
COPY src/main ./src/main

# Régénérer les stubs avec le JDK de cette image
RUN mkdir -p src/generated && \
    idlj -fall -td src/generated src/main/java/com/pdfcorba/PDFService.idl


# Compiler le projet
RUN mvn package -DskipTests

# Exposer le port CORBA Name Service
EXPOSE 1050

# Démarrer le Name Service puis le serveur CORBA
CMD orbd -ORBInitialPort 1050 & \
    sleep 3 && \
    java -cp target/corba-server-1.0-SNAPSHOT-jar-with-dependencies.jar \
         com.pdfcorba.CORBAServer \
         -ORBInitialHost localhost \
         -ORBInitialPort 1050 
