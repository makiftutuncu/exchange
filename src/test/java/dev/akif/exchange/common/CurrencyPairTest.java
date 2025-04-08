package dev.akif.exchange.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpStatusCodeException;

public class CurrencyPairTest {
  @Test
  @DisplayName("validating and creating a currency pair")
  void validatingAndCreatingACurrencyPair() {
    var e1 = assertThrows(HttpStatusCodeException.class, () -> CurrencyPair.of("", ""));
    assertEquals(Errors.Common.invalidCurrency("source", "").getMessage(), e1.getMessage());

    var e2 = assertThrows(HttpStatusCodeException.class, () -> CurrencyPair.of("FOO", ""));
    assertEquals(Errors.Common.invalidCurrency("source", "FOO").getMessage(), e2.getMessage());

    var e3 = assertThrows(HttpStatusCodeException.class, () -> CurrencyPair.of("USD", ""));
    assertEquals(Errors.Common.invalidCurrency("target", "").getMessage(), e3.getMessage());

    var e4 = assertThrows(HttpStatusCodeException.class, () -> CurrencyPair.of("USD", "FOO"));
    assertEquals(Errors.Common.invalidCurrency("target", "FOO").getMessage(), e4.getMessage());

    assertDoesNotThrow(() -> CurrencyPair.of("USD", "TRY"));
  }
}
