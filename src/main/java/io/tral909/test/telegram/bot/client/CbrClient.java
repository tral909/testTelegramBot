package io.tral909.test.telegram.bot.client;

import io.tral909.test.telegram.bot.dto.ValCurs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CbrClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cbr.exchange-rates.url}")
    private String exchangeRatesUrl;

    @Cacheable("exchange-rates")
    public ValCurs getExchangeRates() {
        return restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
    }
}