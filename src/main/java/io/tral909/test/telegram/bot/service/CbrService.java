package io.tral909.test.telegram.bot.service;

import io.tral909.test.telegram.bot.dto.ValCurs;
import io.tral909.test.telegram.bot.dto.ValuteDto;
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

    public ValCurs getExchangeRatesMainValutes() {
        var response = restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
        response.setValute(response.getValute().stream()
                .filter(v -> MAIN_VALUTE_CODES.contains(v.getCharCode().toUpperCase(Locale.ROOT)))
                .toList());
        return response;
    }

    public ValuteDto getExchangeRatesByCharCode(String charCode) {
        var response = restTemplate.getForEntity(exchangeRatesUrl, ValCurs.class).getBody();
        ValCurs.Valute foundValute = response.getValute().stream()
                .filter(v -> v.getCharCode().equalsIgnoreCase(charCode))
                .findAny()
                .orElseThrow(); //todo add better exception

        return ValuteDto.builder()
                .date(response.getDate())
                .valute(ValuteDto.Valute.builder()
                        .charCode(foundValute.getCharCode())
                        .name(foundValute.getName())
                        .nominal(foundValute.getNominal())
                        .value(foundValute.getValue())
                        .vunitRate(foundValute.getVunitRate())
                        .build())
                .build();
    }
}
