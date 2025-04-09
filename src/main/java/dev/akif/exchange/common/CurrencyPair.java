package dev.akif.exchange.common;

import java.util.Currency;

public record CurrencyPair(String source, String target) {
  public static CurrencyPair of(String source, String target) {
    try {
      Currency.getInstance(source);
    } catch (Exception e) {
      throw Errors.Common.invalidCurrency("source", source);
    }
    try {
      Currency.getInstance(target);
    } catch (Exception e) {
      throw Errors.Common.invalidCurrency("target", target);
    }
    return new CurrencyPair(source, target);
  }
}
