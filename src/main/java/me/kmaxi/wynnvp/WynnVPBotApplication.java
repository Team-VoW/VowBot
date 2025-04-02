package me.kmaxi.wynnvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WynnVPBotApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WynnVPBotApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}