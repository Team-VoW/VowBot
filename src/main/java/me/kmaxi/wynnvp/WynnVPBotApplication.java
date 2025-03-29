package me.kmaxi.wynnvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WynnVPBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(WynnVPBotApplication.class, args);
    }
}