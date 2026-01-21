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
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install native dependencies
RUN apt-get update && apt-get install -y \
    ffmpeg \
    curl \
    ca-certificates \
    python3 \
    && rm -rf /var/lib/apt/lists/*

# Install yt-dlp
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
    -o /usr/local/bin/yt-dlp && chmod +x /usr/local/bin/yt-dlp

ENV TZ=UTC

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9000

# Prevent OOM on Render free tier
ENV JAVA_OPTS="-Xms128m -Xmx512m"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]