package io.tral909.test.telegram.bot;

import io.tral909.test.telegram.bot.client.CbrClient;
import io.tral909.test.telegram.bot.dto.ValCurs;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CbrControllerTest {

    @MockBean
    private CbrClient cbrClient;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getExchangeRatesSuccess() throws Exception {
        var valCurs = new ValCurs();
        valCurs.setDate("16.09.1992");

        var valute1 = new ValCurs.Valute();
        valute1.setCharCode("USD");
        valute1.setName("Доллар США");
        valute1.setVunitRate("91,2");
        valute1.setValue("91,2");
        valute1.setNominal("1");
        var valute2 = new ValCurs.Valute();
        valute2.setCharCode("EUR");
        valute2.setName("Евро");
        valute2.setVunitRate("101,2");
        valute2.setValue("101,2");
        valute2.setNominal("1");

        valCurs.setValute(List.of(valute1, valute2));

        Mockito.when(cbrClient.getExchangeRates()).thenReturn(valCurs);

        mockMvc.perform(get("/exchange-rates"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is("16.09.1992")))
                .andExpect(jsonPath("$.valute[0].charCode", is("USD")))
                .andExpect(jsonPath("$.valute[0].name", is("Доллар США")))
                .andExpect(jsonPath("$.valute[0].nominal", is("1")))
                .andExpect(jsonPath("$.valute[0].value", is("91,2")))
                .andExpect(jsonPath("$.valute[0].vunitRate", is("91,2")))
                .andExpect(jsonPath("$.valute[1].charCode", is("EUR")))
                .andExpect(jsonPath("$.valute[1].name", is("Евро")))
                .andExpect(jsonPath("$.valute[1].nominal", is("1")))
                .andExpect(jsonPath("$.valute[1].value", is("101,2")))
                .andExpect(jsonPath("$.valute[1].vunitRate", is("101,2")));

        Mockito.verify(cbrClient).getExchangeRates();
    }

    @Test
    void getExchangeRatesMainValutesSuccess() throws Exception {
        var valCurs = new ValCurs();
        valCurs.setDate("16.09.1992");

        var valute1 = new ValCurs.Valute();
        valute1.setCharCode("USD");
        valute1.setName("Доллар США");
        valute1.setVunitRate("91,2");
        valute1.setValue("91,2");
        valute1.setNominal("1");
        var valute2 = new ValCurs.Valute();
        valute2.setCharCode("EUR");
        valute2.setName("Евро");
        valute2.setVunitRate("101,2");
        valute2.setValue("101,2");
        valute2.setNominal("1");
        var valute3 = new ValCurs.Valute();
        valute3.setCharCode("BYN");
        valute3.setName("Белорусский рубль");
        valute3.setVunitRate("28,3075");
        valute3.setValue("28,3075");
        valute3.setNominal("1");
        var valute4 = new ValCurs.Valute();
        valute4.setCharCode("AUD");
        valute4.setName("Австралийский доллар");
        valute4.setVunitRate("61,9789");
        valute4.setValue("61,9789");
        valute4.setNominal("1");

        valCurs.setValute(List.of(valute1, valute2, valute3, valute4));

        Mockito.when(cbrClient.getExchangeRates()).thenReturn(valCurs);

        mockMvc.perform(get("/exchange-rates/main-valutes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is("16.09.1992")))
                .andExpect(jsonPath("$.valute[0].charCode", is("USD")))
                .andExpect(jsonPath("$.valute[0].name", is("Доллар США")))
                .andExpect(jsonPath("$.valute[0].nominal", is("1")))
                .andExpect(jsonPath("$.valute[0].value", is("91,2")))
                .andExpect(jsonPath("$.valute[0].vunitRate", is("91,2")))
                .andExpect(jsonPath("$.valute[1].charCode", is("EUR")))
                .andExpect(jsonPath("$.valute[1].name", is("Евро")))
                .andExpect(jsonPath("$.valute[1].nominal", is("1")))
                .andExpect(jsonPath("$.valute[1].value", is("101,2")))
                .andExpect(jsonPath("$.valute[1].vunitRate", is("101,2")))
                .andExpect(jsonPath("$.valute[2].charCode", is("BYN")))
                .andExpect(jsonPath("$.valute[2].name", is("Белорусский рубль")))
                .andExpect(jsonPath("$.valute[2].nominal", is("1")))
                .andExpect(jsonPath("$.valute[2].value", is("28,3075")))
                .andExpect(jsonPath("$.valute[2].vunitRate", is("28,3075")));

        Mockito.verify(cbrClient).getExchangeRates();
    }

    //todo add remaining 1 test
}
