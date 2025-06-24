package me.kmaxi.wynnvp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * DotenvEnvironmentPostProcessor loads environment variables from a .env file
 * and injects them into the Spring Environment before the application starts.
 *
 * <p>In development, it loads the .env file from the current working directory.
 * In production, place the .env file in the same directory as the JAR file.
 *
 * <p>To enable this post-processor, it is registered it in META-INF/spring.factories:

 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> envMap.put(entry.getKey(), entry.getValue()));

        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));
    }
}