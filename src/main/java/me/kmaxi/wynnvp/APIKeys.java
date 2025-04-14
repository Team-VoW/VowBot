package me.kmaxi.wynnvp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class APIKeys {

    @Value("${bot.token}")
    public String botToken;

    @Value("${api.reading.key}")
    public String readingApiKey;

    @Value("${api.update.key}")
    public String updateApiKey;

    @Value("${api.discord_integration.key}")
    public String discordIntegrationApiKey;

    @Value("${sql.url}")
    public String sqlUrl;

    @Value("${sql.username}")
    public String sqlUsername;

    @Value("${sql.password}")
    public String sqlPassword;
}