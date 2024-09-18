package io.tral909.test.telegram.bot.service;

import io.tral909.test.telegram.bot.client.CbrClient;
import io.tral909.test.telegram.bot.dto.ValCurs;
import io.tral909.test.telegram.bot.dto.ValuteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CbrService {

    private final static List<String> MAIN_VALUTE_CODES = List.of("USD", "EUR", "BYN");

    @Autowired
    private CbrClient cbrClient;

    public ValCurs getExchangeRates() {
        return cbrClient.getExchangeRates();
    }

    @Cacheable("exchange-rates-main-valutes")
    public ValCurs getExchangeRatesMainValutes() {
        var response = cbrClient.getExchangeRates();
        response.setValute(response.getValute().stream()
                .filter(v -> MAIN_VALUTE_CODES.contains(v.getCharCode().toUpperCase(Locale.ROOT)))
                .toList());
        return response;
    }

    @Cacheable("exchange-rates-by-char-code")
    public ValuteDto getExchangeRatesByCharCode(String charCode) {
        var response = cbrClient.getExchangeRates();
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
