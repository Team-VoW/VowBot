FROM openjdk:17-jdk-slim

# Install FFmpeg for audio conversion
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file and env file into the container
COPY vowbot.jar app.jar
COPY .env .env

# Set the main command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
