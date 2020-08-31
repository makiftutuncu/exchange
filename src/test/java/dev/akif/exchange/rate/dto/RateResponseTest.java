package dev.akif.exchange.rate.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.akif.exchange.common.CurrencyPair;

public class RateResponseTest {
    @Test
    @DisplayName("creating rate response")
    void creatingRateResponse() {
        RateResponse response = new RateResponse(new CurrencyPair("USD", "TRY"), 7.0);

        assertEquals("USD", response.source);
        assertEquals("TRY", response.target);
        assertEquals(7.0,   response.rate);
    }
}
