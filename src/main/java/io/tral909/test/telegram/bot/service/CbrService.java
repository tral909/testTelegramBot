package io.tral909.test.telegram.bot.service;

import io.tral909.test.telegram.bot.dto.ValCurs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;

@Service
public class CbrService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final static List<String> MAIN_VALUTE_CODES = List.of("USD", "EUR", "BYN");

    @Value("${cbr.exchange-rates.url}")
    private String exchangeRatesUrl;

    public ValCurs getExchangeRates() {
        return restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
    }

    public List<ValCurs.Valute> getExchangeRatesMainValutes() {
        var response = restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
        return response.getValute().stream()
                .filter(v -> MAIN_VALUTE_CODES.contains(v.getCharCode().toUpperCase(Locale.ROOT)))
                .toList();
    }

    public ValCurs.Valute getExchangeRatesByCharCode(String charCode) {
        var response = restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
        return response.getValute().stream()
                .filter(v -> v.getCharCode().equalsIgnoreCase(charCode))
                .findAny()
                .orElse(null);
    }
}
