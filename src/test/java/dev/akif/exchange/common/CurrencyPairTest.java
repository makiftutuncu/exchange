package dev.akif.exchange.common;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import e.java.E;
import e.java.EOr;

public class CurrencyPairTest {
    @Test
    @DisplayName("validating and creating a currency pair")
    void validatingAndCreatingACurrencyPair() {
        E e = Errors.Common.invalidCurrency;

        EOr<CurrencyPair> pair1 = CurrencyPair.of("", "");

        assertEquals(e.data("source", "").toEOr(), pair1);

        EOr<CurrencyPair> pair2 = CurrencyPair.of("FOO", "");

        assertEquals(e.data("source", "FOO").toEOr(), pair2);

        EOr<CurrencyPair> pair3 = CurrencyPair.of("USD", "");

        assertEquals(e.data("target", "").toEOr(), pair3);

        EOr<CurrencyPair> pair4 = CurrencyPair.of("USD", "FOO");

        assertEquals(e.data("target", "FOO").toEOr(), pair4);

        EOr<CurrencyPair> pair5 = CurrencyPair.of("USD", "TRY");

        assertTrue(pair5.hasValue());
        assertEquals(new CurrencyPair("USD", "TRY"), pair5.value().get());
    }
}
