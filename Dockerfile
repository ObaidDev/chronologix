##############################################################
#      First stage: Dependencies resolution and caching      #
##############################################################

FROM maven:3.9.8-eclipse-temurin-17-alpine AS dependencies

WORKDIR /opt/app

# Copy only pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

###########################################
#      Second stage: Build application    #
###########################################

FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder

WORKDIR /opt/app

# Copy dependencies from previous stage
COPY --from=dependencies /root/.m2 /root/.m2

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application
ENV APP_JAR_NAME=chronologix-0.0.1-SNAPSHOT.jar
RUN mvn package -DskipTests -B && \
    mkdir -p target/extracted && \
    java -Djarmode=layertools -jar target/${APP_JAR_NAME} extract --destination target/extracted

##############################################
#      Third stage: Create custom JRE        #
##############################################

FROM eclipse-temurin:17-jdk-alpine AS jre-builder

WORKDIR /opt/app

# Install required tools
RUN apk update && \
    apk add --no-cache binutils

# Copy the built JAR
ENV APP_JAR_NAME=chronologix-0.0.1-SNAPSHOT.jar
COPY --from=builder /opt/app/target/${APP_JAR_NAME} /opt/app/app.jar

# Analyze dependencies and create module list
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 17 \
    --print-module-deps \
    /opt/app/app.jar > modules.txt

# Create optimized JRE
RUN "$JAVA_HOME"/bin/jlink \
    --add-modules $(cat modules.txt),java.base,java.logging,java.xml,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /optimized-jdk-17


#################################################
#      Final stage: Runtime image               #
#################################################

FROM alpine:3.20

# Install required runtime packages and security updates
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        ca-certificates \
        curl \
        tzdata && \
    rm -rf /var/cache/apk/*

# Set environment variables
ENV JAVA_HOME=/opt/jdk/jdk-17
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV SPRING_PROFILES_ACTIVE=prod

# Copy JRE from builder stage
COPY --from=jre-builder /optimized-jdk-17 $JAVA_HOME

# Create application user
ARG APPLICATION_USER=spring
RUN addgroup --system "${APPLICATION_USER}" && \
    adduser --system "${APPLICATION_USER}" --ingroup "${APPLICATION_USER}" && \
    mkdir -p /app && \
    chown -R "${APPLICATION_USER}:${APPLICATION_USER}" /app

# Copy application layers for better caching
COPY --from=builder --chown="${APPLICATION_USER}:${APPLICATION_USER}" /opt/app/target/extracted/dependencies/ /app/
COPY --from=builder --chown="${APPLICATION_USER}:${APPLICATION_USER}" /opt/app/target/extracted/spring-boot-loader/ /app/
COPY --from=builder --chown="${APPLICATION_USER}:${APPLICATION_USER}" /opt/app/target/extracted/snapshot-dependencies/ /app/
COPY --from=builder --chown="${APPLICATION_USER}:${APPLICATION_USER}" /opt/app/target/extracted/application/ /app/

WORKDIR /app

# Switch to non-root user
USER "${APPLICATION_USER}"

# Add health check (adjust the endpoint based on your application)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# Expose port
EXPOSE 8080

# Optimized JVM arguments for container environments
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-XX:+OptimizeStringConcat", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.jmx.enabled=false", \
    "org.springframework.boot.loader.launch.JarLauncher"]