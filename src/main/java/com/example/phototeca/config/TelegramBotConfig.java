package com.example.phototeca.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class TelegramBotConfig {

    @Value("${bot.name}")
    private String name;

    @Value("$bot.token}")
    private String token;

    @Value("$bot.chatId")
    private String chatId;

}
