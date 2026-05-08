# Java 8 pour compatibilité CORBA
FROM maven:3.8.6-openjdk-8

WORKDIR /app

COPY pom.xml .
COPY src/main ./src/main
COPY src/main/java/com/pdfcorba/PDFService.idl ./PDFService.idl


# Régénérer les stubs avec le JDK de cette image
RUN mkdir -p src/main/java/PDFServiceModule && \
    idlj -fall -td src/main/java src/main/java/com/pdfcorba/PDFService.idl


# Compiler
RUN mvn package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/spring-client-1.0-SNAPSHOT.jar"]
