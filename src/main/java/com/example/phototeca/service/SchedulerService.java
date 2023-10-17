package com.example.phototeca.service;

import com.example.phototeca.CryptocurrencyRepository;
import com.example.phototeca.model.Cryptocurrency;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private static final String PATH = "https://api.mexc.com/api/v3/ticker/price";

    private final CryptocurrencyRepository cryptocurrencyRepository;
    private final TelegramBotService telegramBotService;

    @Scheduled(fixedRate = 1000)
    public void cryptoUpdateScheduler() throws IOException {
        log.info("Scheduler updating cryptocurrencies.");
        final String jsonAsString = readFile();
        List<Cryptocurrency> cryptocurrencies = convertToObject(jsonAsString);
        updateCryptocurrencies(cryptocurrencies);
    }

    @Transactional
    public void updateCryptocurrencies(final List<Cryptocurrency> cryptocurrencies) {
        if (cryptocurrencies != null) {
            for (Cryptocurrency cryptocurrency : cryptocurrencies) {
                Cryptocurrency cryptocurrencyBySymbol = cryptocurrencyRepository.findBySymbol(cryptocurrency.getSymbol());
                if (cryptocurrencyBySymbol != null) {
                    final double percent = (double) cryptocurrencyBySymbol.getPrice() / cryptocurrency.getPrice();
                    if (percent != 0) {
                        final String messageToUser = percent > 0.0 ?
                                "Cryptocurrency " + cryptocurrency.getSymbol() + " becomes more expensive by more than " + percent + "  percent." :
                                "Cryptocurrency " + cryptocurrency.getSymbol() + " becomes more cheaper by more than " + percent + "  percent.";
                        sendMessageToUser(messageToUser);

                        cryptocurrencyRepository.delete(cryptocurrencyBySymbol);
                        cryptocurrencyRepository.save(cryptocurrency);
                    }
                } else {
                    cryptocurrencyRepository.save(cryptocurrency);
                }
            }
        }
    }

    public void sendMessageToUser(final long chatId, final String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {

        }
    }

    private String readFile() throws IOException {
        final URL url = new URL(PATH);
        try (InputStream inputStream = url.openStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final StringBuilder builder = new StringBuilder();
            int c;
            while ((c = bufferedReader.read()) != -1) {
                builder.append((char) c);
            }
            return builder.toString();
        }
    }

    private List<Cryptocurrency> convertToObject(final String jsonAsString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonAsString, new TypeReference<List<Cryptocurrency>>() {
        });
    }
}
