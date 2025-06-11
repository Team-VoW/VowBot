# Use a lightweight OpenJDK image with Java 14
FROM openjdk:16-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file and env file into the container
COPY vowbot.jar app.jar
COPY .env .env

# Set the main command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
