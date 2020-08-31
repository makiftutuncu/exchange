package dev.akif.exchange.conversion.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.akif.exchange.common.CurrencyPair;

public class ConversionTest {
    @Test
    @DisplayName("creating conversion response from conversion")
    void creatingConversionResponseFromConversion() {
        Conversion conversion = new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L);

        assertEquals(-1L,        conversion.getId());
        assertEquals("USD",      conversion.getSource());
        assertEquals(10.0,       conversion.getSourceAmount());
        assertEquals("TRY",      conversion.getTarget());
        assertEquals(70.0,       conversion.getTargetAmount());
        assertEquals(7.0,        conversion.getRate());
        assertEquals(123456789L, conversion.getCreatedAt());
    }
}
