# Utiliser une image Maven pour construire l'application
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Copier les fichiers de configuration Maven d'abord (pour optimiser le cache)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copier le code source et construire le JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Utiliser une image JDK plus légère pour l'exécution
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar
# Exposer le port de l'application
EXPOSE 8080
# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
