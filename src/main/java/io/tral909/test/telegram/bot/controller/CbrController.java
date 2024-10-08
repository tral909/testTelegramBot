package io.tral909.test.telegram.bot.controller;

import io.tral909.test.telegram.bot.dto.ValCurs;
import io.tral909.test.telegram.bot.dto.ValuteDto;
import io.tral909.test.telegram.bot.service.CbrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class CbrController {

    private final CbrService cbrService;

    @GetMapping("exchange-rates")
    public ValCurs getExchangeRates() {
        return cbrService.getExchangeRates();
    }

    @GetMapping("exchange-rates/main-valutes")
    public ValCurs getExchangeRatesMainValutes() {
        return cbrService.getExchangeRatesMainValutes();
    }

    @GetMapping("exchange-rates/{charCode}")
    public ValuteDto getExchangeRatesByCharCode(@PathVariable String charCode) {
        return cbrService.getExchangeRatesByCharCode(charCode);
    }

}
