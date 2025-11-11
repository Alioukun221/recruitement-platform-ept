# ---------- STAGE 1 : BUILD ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copier pom.xml pour télécharger les dépendances d’abord (cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Build du projet
RUN mvn clean package -DskipTests


# ---------- STAGE 2 : RUN ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Définir le port exposé
EXPOSE 8080

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
