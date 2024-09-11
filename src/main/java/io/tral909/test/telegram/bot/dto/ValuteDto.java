package io.tral909.test.telegram.bot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValuteDto {

    private String date;
    private Valute valute;

    @Data
    @Builder
    public static class Valute {
        private String charCode;
        private String name;
        private String nominal;
        private String value;
        private String vunitRate;
    }
}
