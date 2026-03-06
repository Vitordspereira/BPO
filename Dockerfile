# Estágio 1: Build da aplicação
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
# Adicionamos a flag de encoding aqui para evitar o erro MalformedInput
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Estágio 2: Execução da aplicação
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]