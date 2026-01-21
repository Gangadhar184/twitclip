# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies first (Docker cache friendly)
RUN ./mvnw -B dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -B clean package -DskipTests


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install required native tools
RUN apk add --no-cache ffmpeg curl

# Install yt-dlp
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
    -o /usr/local/bin/yt-dlp && chmod +x /usr/local/bin/yt-dlp

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9000

CMD ["java", "-jar", "app.jar"]
