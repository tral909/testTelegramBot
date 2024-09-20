package io.tral909.test.telegram.bot.configuration;

import io.tral909.test.telegram.bot.dto.ValCurs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Value("${cbr.exchange-rates.url}")
    private String exchangeRatesUrl;

    @Scheduled(cron = "5 0 0 * * MON-FRI")
    //@Scheduled(fixedDelay = 10_000)
    @CachePut("exchange-rates")
    public ValCurs scheduledGetExchangeRates() {
        return restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStartContextGetExchangeRates() {
        //log.error("CONTEXT REFRESHED");
        ValCurs valCurs = restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
        var cache = cacheManager.getCache("exchange-rates");
        cache.put(SimpleKey.EMPTY, valCurs);
    }
}