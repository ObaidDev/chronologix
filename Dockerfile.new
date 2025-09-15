# First stage, build the custom JRE
FROM eclipse-temurin:17-jdk-alpine AS jre-builder

RUN mkdir /opt/app
COPY . /opt/app

WORKDIR /opt/app

# Install required packages
RUN apk update && \
    apk add --no-cache tar binutils

# Build the application with Gradle
RUN ./gradlew build -x test

# Extract the built JAR to analyze dependencies
RUN jar xvf build/libs/chronologix-0.0.1-SNAPSHOT.jar

# Analyze dependencies and create modules list
RUN jdeps --ignore-missing-deps -q  \
    --recursive  \
    --multi-release 17  \
    --print-module-deps  \
    --class-path 'BOOT-INF/lib/*'  \
    build/libs/chronologix-0.0.1-SNAPSHOT.jar > modules.txt

# Build small JRE image
RUN "$JAVA_HOME/bin/jlink" \
         --verbose \
         --add-modules $(cat modules.txt) \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /optimized-jdk-17

# Clean up Gradle cache and extracted files after build
RUN ./gradlew build -x test --no-daemon --build-cache && \
    rm -rf /root/.gradle && \
    rm -rf build/classes build/resources build/tmp

# Second stage, Use the custom JRE and build the app image
FROM alpine:3.19
ENV JAVA_HOME=/opt/jdk/jdk-17
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the base image
COPY --from=jre-builder /optimized-jdk-17 $JAVA_HOME

# Add app user
ARG APPLICATION_USER=spring

# Create a user to run the application, don't run as root, and create the application directory
RUN addgroup --system "$APPLICATION_USER" && \
    adduser --system "$APPLICATION_USER" --ingroup "$APPLICATION_USER" && \
    mkdir /app && \
    chown -R "$APPLICATION_USER" /app

# Copy the JAR from the Gradle build
COPY --from=jre-builder --chown=$APPLICATION_USER:$APPLICATION_USER /opt/app/build/libs/chronologix-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

USER $APPLICATION_USER

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]

# Add metadata labels
LABEL maintainer="your-email@example.com"
LABEL version="0.0.1"