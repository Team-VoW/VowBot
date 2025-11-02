# Voices of Wynn Discord Bot

A Discord bot built with Java, Discord JDA and Spring Boot.

## Setup

### Environment Variables

The bot requires a `.env` file in the project root or next to the .jar with the following variables:

```
BOT_TOKEN=your_discord_bot_token
READING_API_KEY=your_reading_api_key
UPDATE_API_KEY=your_update_api_key
DISCORD_INTEGRATION_API_KEY=your_discord_integration_api_key
SQL_URL=your_sql_connection_url
SQL_USERNAME=your_sql_username
SQL_PASSWORD=your_sql_password
```

### Building the Project

```bash
./gradlew build
```

### FFmpeg Requirement

The bot requires **FFmpeg** to convert audition audio files to MP3 format:

- **Windows:** Download from [gyan.dev/ffmpeg](https://www.gyan.dev/ffmpeg/builds/) and add to PATH
- **Linux:** `sudo apt-get install ffmpeg` (Debian/Ubuntu) or `sudo yum install ffmpeg` (RHEL/CentOS)
- **Mac:** `brew install ffmpeg`
- **Docker:** Automatically installed via Dockerfile

Verify installation: `ffmpeg -version`

## Running the Application

### Locally

```bash
java -jar build/libs/wynnvp-bot.jar
```

### Docker

The project includes a Dockerfile for containerized deployment:

```bash
docker build -t wynnvp-bot .
docker run -d --name wynnvp-bot --restart unless-stopped wynnvp-bot
```

## Deployment

The project uses GitHub Actions for CI/CD. When changes are pushed to the master branch:
1. The application is built
2. The JAR is renamed to "vowbot.jar"
3. Files are transferred to the configured server
4. The Docker container is rebuilt and restarted

## SonarCloud Analysis

The project uses SonarCloud for continuous code quality monitoring:

- Automated code quality analysis on every push to master branch and pull request
- Detects code smells, bugs, vulnerabilities, and security hotspots
- Tracks test coverage and code duplication
- Enforces coding standards and best practices

The analysis workflow runs automatically via GitHub Actions and uses Gradle for integration. The configuration can be found in `.github/workflows/sonar-cloud-analysis.yml`.

