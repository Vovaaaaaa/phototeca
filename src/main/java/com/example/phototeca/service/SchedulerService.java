package com.example.phototeca.service;

import com.example.phototeca.model.Cryptocurrency;
import com.example.phototeca.model.User;
import com.example.phototeca.repository.CryptocurrencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private static final String PATH = "https://api.mexc.com/api/v3/ticker/price";

    private final CryptocurrencyRepository cryptocurrencyRepository;
    private final TelegramBotService telegramBotService;
    private final UserService userService;

    @Scheduled(fixedRate = 1000)
    public void cryptoUpdateScheduler() throws IOException, ParseException {
        log.info("Scheduler updating cryptocurrencies.");
        final String jsonAsString = readFile();
        List<Cryptocurrency> cryptocurrencies = convertToObject(jsonAsString);
        updateCryptocurrencies(cryptocurrencies);
    }

    @Transactional
    public void updateCryptocurrencies(final List<Cryptocurrency> cryptocurrencies) throws ParseException {
        if (cryptocurrencies != null) {
            for (Cryptocurrency cryptocurrency : cryptocurrencies) {
                final Cryptocurrency cryptocurrencyBySymbol = cryptocurrencyRepository.findBySymbol(cryptocurrency.getSymbol());
                if (cryptocurrencyBySymbol != null) {
                    final double percent = (double) cryptocurrencyBySymbol.getPrice() / cryptocurrency.getPrice();
                    if (percent != 0.0) {
                        final String messageToUser = percent > 0.0 ?
                                "Cryptocurrency " + cryptocurrency.getSymbol() + " becomes more expensive by more than " + percent + "  percent." :
                                "Cryptocurrency " + cryptocurrency.getSymbol() + " becomes more cheaper by more than " + percent + "  percent.";
                        final List<User> users = userService.findAll();
                        final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                        final Date currentTime = new Date(formatter.format(Calendar.getInstance().getTime()));
                        for (User user : users) {
                            if (currentTime.after(formatter.parse(String.valueOf(user.getWorkStartTime().getHour())))) {
                                log.info("Notify user with id: " + user.getId());
                                telegramBotService.sendMessage(user.getChatId(), messageToUser);
                            }
                        }
                        cryptocurrencyRepository.delete(cryptocurrencyBySymbol);
                        log.info("Updated crypto with symbol: " + cryptocurrency.getSymbol());
                        cryptocurrencyRepository.save(cryptocurrency);
                    }
                } else {
                    cryptocurrencyRepository.save(cryptocurrency);
                }
            }
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
