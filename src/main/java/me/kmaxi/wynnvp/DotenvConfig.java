package me.kmaxi.wynnvp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        System.out.println("Env: " + System.getProperty("user.dir"));
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir")) // Directory of the JAR
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}