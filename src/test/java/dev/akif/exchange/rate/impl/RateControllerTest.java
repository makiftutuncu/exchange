package dev.akif.exchange.rate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.TimeProvider;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import e.java.E;
import e.java.EOr;

@SpringBootTest
@AutoConfigureMockMvc
public class RateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateService rateService;

    @MockBean
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        Mockito.when(timeProvider.now()).thenReturn(123456789L);
    }

    @Test
    @DisplayName("getting rates fails with invalid input")
    void gettingRatesFailsWithInvalidInput() throws Exception {
        MockHttpServletResponse response = perform(ratesRequest("foo", ""));

        E expected = Errors.Common.invalidCurrency.data("source", "foo").time(123456789L);

        assertEquals(expected.code().get(), response.getStatus());
        assertEquals(expected.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("getting rates fails when service fails")
    void gettingRatesFailsWhenServiceFails() throws Exception {
        CurrencyPair pair = new CurrencyPair("USD", "TRY");

        E e = Errors.Rate.cannotReadRate.time(123456789L);

        Mockito.when(rateService.rate(pair)).thenReturn(e.toEOr());

        MockHttpServletResponse response = perform(ratesRequest("USD", "TRY"));

        assertEquals(e.code().get(), response.getStatus());
        assertEquals(e.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("getting rates returns rates")
    void gettingRatesReturnsRates() throws Exception {
        CurrencyPair pair = new CurrencyPair("USD", "TRY");

        RateResponse expected = new RateResponse(pair, 7.0);

        Mockito.when(rateService.rate(pair)).thenReturn(EOr.from(expected));

        MockHttpServletResponse response = perform(ratesRequest("USD", "TRY"));

        assertEquals(200, response.getStatus());
        assertEquals(expected.toString(), response.getContentAsString());
    }

    private MockHttpServletRequestBuilder ratesRequest(String source, String target) {
        return MockMvcRequestBuilders
                .get("/rates")
                .param("source", source)
                .param("target", target);
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn().getResponse();
    }
}
