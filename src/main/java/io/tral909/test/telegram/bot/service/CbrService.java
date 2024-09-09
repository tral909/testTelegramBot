package io.tral909.test.telegram.bot.service;

import io.tral909.test.telegram.bot.dto.ValCurs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CbrService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${cbr.exchange-rates.url}")
    private String exchangeRatesUrl;

    public ValCurs getExchangeRates() {
        return restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
    }

    public ValCurs.Valute getExchangeRatesByCharCode(String charCode) {
        var response = restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
        return response.getValute().stream()
                .filter(v -> v.getCharCode().equalsIgnoreCase(charCode))
                .findAny()
                .orElse(null);
    }
}
