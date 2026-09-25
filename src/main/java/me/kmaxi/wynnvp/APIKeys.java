package me.kmaxi.wynnvp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class APIKeys {

    @Value("${bot.token}")
    public String botToken;

    @Value("${api.key}")
    public String botApiKey;

    @Value("${sql.url}")
    public String sqlUrl;

    @Value("${sql.username}")
    public String sqlUsername;

    @Value("${sql.password}")
    public String sqlPassword;
}