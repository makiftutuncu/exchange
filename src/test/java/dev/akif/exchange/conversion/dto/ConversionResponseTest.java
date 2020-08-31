package dev.akif.exchange.conversion.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.conversion.model.Conversion;

public class ConversionResponseTest {
    @Test
    @DisplayName("creating conversion response from conversion")
    void creatingConversionResponseFromConversion() {
        Conversion conversion = new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L);
        ConversionResponse response = new ConversionResponse(conversion);

        assertEquals(-1L,        response.id);
        assertEquals("USD",      response.source);
        assertEquals(10.0,       response.sourceAmount);
        assertEquals("TRY",      response.target);
        assertEquals(70.0,       response.targetAmount);
        assertEquals(7.0,        response.rate);
        assertEquals(123456789L, response.createdAt);
    }
}
