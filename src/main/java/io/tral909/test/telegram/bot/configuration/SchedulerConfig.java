package io.tral909.test.telegram.bot.configuration;

import io.tral909.test.telegram.bot.dto.ValCurs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cbr.exchange-rates.url}")
    private String exchangeRatesUrl;

    @Scheduled(cron = "5 0 0 * * MON-FRI")
    //@Scheduled(fixedDelay = 10_000)
    @CachePut("exchange-rates")
    public ValCurs getExchangeRates() {
        return restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
    }
}